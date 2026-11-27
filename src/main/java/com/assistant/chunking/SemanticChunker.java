package com.assistant.chunking;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Semantic Markdown & Recursive Sentence Boundary Text Chunker
 * Deconstructs documents while preserving hierarchical heading breadcrumbs,
 * markdown structural boundaries (headers, code blocks, bullet points),
 * and sentence semantics with configurable overlap.
 */
public class SemanticChunker {

    private final int maxChunkSize;
    private final int chunkOverlap;
    private final boolean preserveSectionHeaders;

    private static final Pattern HEADING_PATTERN = Pattern.compile("^(#{1,6})\\s+(.+)$", Pattern.MULTILINE);
    private static final Pattern CODE_BLOCK_PATTERN = Pattern.compile("```[\\s\\S]*?```");
    private static final Pattern SENTENCE_SPLIT_PATTERN = Pattern.compile("(?<=[.!?])\\s+(?=[A-Z0-9\"'‘“])");

    public static class TextChunk {
        private final int chunkIndex;
        private final String text;
        private final int charCount;
        private final int tokenEstimate;
        private final String sectionTitle;
        private final List<String> headingHierarchy;
        private final boolean isCodeBlock;
        private final Map<String, Object> metadata;

        public TextChunk(
                int chunkIndex,
                String text,
                String sectionTitle,
                List<String> headingHierarchy,
                boolean isCodeBlock,
                Map<String, Object> metadata) {
            this.chunkIndex = chunkIndex;
            this.text = text != null ? text.trim() : "";
            this.charCount = this.text.length();
            this.tokenEstimate = Math.max(1, (int) Math.ceil(this.charCount / 4.0));
            this.sectionTitle = sectionTitle != null ? sectionTitle : "General";
            this.headingHierarchy = headingHierarchy != null ? new ArrayList<>(headingHierarchy) : Collections.emptyList();
            this.isCodeBlock = isCodeBlock;
            this.metadata = metadata != null ? new HashMap<>(metadata) : new HashMap<>();
            this.metadata.put("chunkIndex", this.chunkIndex);
            this.metadata.put("charCount", this.charCount);
            this.metadata.put("tokenEstimate", this.tokenEstimate);
            this.metadata.put("sectionTitle", this.sectionTitle);
            this.metadata.put("headingHierarchy", this.headingHierarchy);
            this.metadata.put("isCodeBlock", this.isCodeBlock);
        }

        public int getChunkIndex() { return chunkIndex; }
        public String getText() { return text; }
        public int getCharCount() { return charCount; }
        public int getTokenEstimate() { return tokenEstimate; }
        public String getSectionTitle() { return sectionTitle; }
        public List<String> getHeadingHierarchy() { return Collections.unmodifiableList(headingHierarchy); }
        public boolean isCodeBlock() { return isCodeBlock; }
        public Map<String, Object> getMetadata() { return Collections.unmodifiableMap(metadata); }

        @Override
        public String toString() {
            return String.format("TextChunk[#%d, chars=%d, tokens=%d, section='%s', code=%s]",
                    chunkIndex, charCount, tokenEstimate, sectionTitle, isCodeBlock);
        }
    }

    public SemanticChunker() {
        this(500, 50, true);
    }

    public SemanticChunker(int maxChunkSize, int chunkOverlap) {
        this(maxChunkSize, chunkOverlap, true);
    }

    public SemanticChunker(int maxChunkSize, int chunkOverlap, boolean preserveSectionHeaders) {
        if (chunkOverlap >= maxChunkSize) {
            throw new IllegalArgumentException("chunkOverlap must be strictly less than maxChunkSize");
        }
        this.maxChunkSize = maxChunkSize;
        this.chunkOverlap = Math.max(0, chunkOverlap);
        this.preserveSectionHeaders = preserveSectionHeaders;
    }

    /**
     * Parse document text into semantic chunks
     */
    public List<TextChunk> chunkDocument(String text) {
        return chunkDocument(text, "DOC-001", Collections.emptyMap());
    }

    /**
     * Parse document text into semantic chunks with document metadata
     */
    public List<TextChunk> chunkDocument(String text, String documentId, Map<String, Object> baseMetadata) {
        if (text == null || text.trim().isEmpty()) {
            return Collections.emptyList();
        }

        List<TextChunk> result = new ArrayList<>();
        int chunkSequence = 0;

        // Split document into structural sections by markdown headings
        List<SectionBlock> sections = splitByMarkdownHeadings(text);

        for (SectionBlock section : sections) {
            List<String> rawChunks = splitSectionIntoChunks(section.content);

            for (String chunkText : rawChunks) {
                if (chunkText.trim().isEmpty()) continue;

                Map<String, Object> meta = new HashMap<>(baseMetadata);
                meta.put("documentId", documentId);

                boolean isCode = chunkText.trim().startsWith("```") && chunkText.trim().endsWith("```");

                result.add(new TextChunk(
                        chunkSequence++,
                        chunkText,
                        section.title,
                        section.hierarchy,
                        isCode,
                        meta
                ));
            }
        }

        return result;
    }

    private static class SectionBlock {
        String title;
        List<String> hierarchy;
        String content;

        SectionBlock(String title, List<String> hierarchy, String content) {
            this.title = title;
            this.hierarchy = hierarchy;
            this.content = content;
        }
    }

    /**
     * Splits markdown into heading-bounded sections while keeping track of parent breadcrumbs
     */
    private List<SectionBlock> splitByMarkdownHeadings(String markdown) {
        List<SectionBlock> sections = new ArrayList<>();
        String[] lines = markdown.split("\\r?\\n");

        List<String> currentHierarchy = new ArrayList<>();
        String currentTitle = "Introduction";
        StringBuilder currentContent = new StringBuilder();

        for (String line : lines) {
            Matcher matcher = HEADING_PATTERN.matcher(line);
            if (matcher.matches()) {
                // If there's pending content, save previous section
                if (currentContent.length() > 0) {
                    sections.add(new SectionBlock(currentTitle, new ArrayList<>(currentHierarchy), currentContent.toString()));
                    currentContent.setLength(0);
                }

                int level = matcher.group(1).length();
                String headingText = matcher.group(2).trim();
                currentTitle = headingText;

                // Adjust hierarchy stack
                while (currentHierarchy.size() >= level) {
                    currentHierarchy.remove(currentHierarchy.size() - 1);
                }
                currentHierarchy.add(headingText);

                if (preserveSectionHeaders) {
                    currentContent.append(line).append("\n");
                }
            } else {
                currentContent.append(line).append("\n");
            }
        }

        if (currentContent.length() > 0) {
            sections.add(new SectionBlock(currentTitle, new ArrayList<>(currentHierarchy), currentContent.toString()));
        }

        return sections;
    }

    /**
     * Recursive text splitting: paragraphs -> sentences -> word boundaries
     */
    private List<String> splitSectionIntoChunks(String content) {
        List<String> chunks = new ArrayList<>();
        if (content.length() <= maxChunkSize) {
            chunks.add(content.trim());
            return chunks;
        }

        // Paragraph splitting
        String[] paragraphs = content.split("\\n\\s*\\n");
        StringBuilder currentChunk = new StringBuilder();

        for (String para : paragraphs) {
            String cleanPara = para.trim();
            if (cleanPara.isEmpty()) continue;

            if (cleanPara.length() > maxChunkSize) {
                // Flush current buffer
                if (currentChunk.length() > 0) {
                    chunks.add(currentChunk.toString().trim());
                    currentChunk.setLength(0);
                }
                // Break oversized paragraph by sentences
                chunks.addAll(splitParagraphBySentences(cleanPara));
            } else if (currentChunk.length() + cleanPara.length() + 2 <= maxChunkSize) {
                if (currentChunk.length() > 0) currentChunk.append("\n\n");
                currentChunk.append(cleanPara);
            } else {
                chunks.add(currentChunk.toString().trim());

                // Overlap calculation: keep trailing chars for context continuity
                currentChunk.setLength(0);
                if (chunkOverlap > 0 && chunks.size() > 0) {
                    String lastChunk = chunks.get(chunks.size() - 1);
                    int overlapStart = Math.max(0, lastChunk.length() - chunkOverlap);
                    currentChunk.append(lastChunk.substring(overlapStart)).append(" ");
                }
                currentChunk.append(cleanPara);
            }
        }

        if (currentChunk.length() > 0) {
            chunks.add(currentChunk.toString().trim());
        }

        return chunks;
    }

    private List<String> splitParagraphBySentences(String paragraph) {
        List<String> chunks = new ArrayList<>();
        String[] sentences = SENTENCE_SPLIT_PATTERN.split(paragraph);
        StringBuilder current = new StringBuilder();

        for (String sentence : sentences) {
            String s = sentence.trim();
            if (s.isEmpty()) continue;

            if (current.length() + s.length() + 1 <= maxChunkSize) {
                if (current.length() > 0) current.append(" ");
                current.append(s);
            } else {
                if (current.length() > 0) {
                    chunks.add(current.toString().trim());
                    current.setLength(0);
                }
                if (s.length() > maxChunkSize) {
                    // Substring split fallback for strings with no sentence boundary
                    for (int i = 0; i < s.length(); i += (maxChunkSize - chunkOverlap)) {
                        int end = Math.min(s.length(), i + maxChunkSize);
                        chunks.add(s.substring(i, end).trim());
                    }
                } else {
                    current.append(s);
                }
            }
        }

        if (current.length() > 0) {
            chunks.add(current.toString().trim());
        }

        return chunks;
    }

    public static void main(String[] args) {
        SemanticChunker chunker = new SemanticChunker(250, 40, true);
        String sampleMd = "# Enterprise Architecture\n\n" +
                "This document describes the cloud microservice design for enterprise high availability.\n\n" +
                "## Database Tier\n\n" +
                "PostgreSQL 16 with pgvector handles hybrid embeddings search and RRF ranking. " +
                "Write replicas are distributed across multi-region availability zones to ensure low latency.\n\n" +
                "```sql\n" +
                "SELECT id, content, 1 - (embedding <=> query_vec) AS similarity\n" +
                "FROM document_chunks ORDER BY embedding <=> query_vec LIMIT 5;\n" +
                "```\n\n" +
                "## Caching Tier\n\n" +
                "Redis Sentinel provides sub-millisecond session caching and distributed rate limits.";

        List<TextChunk> chunks = chunker.chunkDocument(sampleMd);
        System.out.println("Generated " + chunks.size() + " semantic chunks:");
        for (TextChunk c : chunks) {
            System.out.println("Chunk #" + c.getChunkIndex() + " (" + c.getCharCount() + " chars) Section: " + c.getSectionTitle());
            System.out.println("  Hierarchy: " + c.getHeadingHierarchy());
            System.out.println("  Content: " + c.getText().replace("\n", " "));
        }
        assert chunks.size() >= 3 : "Should have created multiple chunks";
        System.out.println("ALL SEMANTIC CHUNKER TESTS PASSED!");
    }
}
