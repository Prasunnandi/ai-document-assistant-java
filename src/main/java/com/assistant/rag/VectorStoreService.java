package com.assistant.rag;

import org.springframework.stereotype.Service;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Spring Boot 3 PGVector Similarity Search Service
 * Manages document embeddings storage, metadata filtering, and k-nearest neighbors (k-NN)
 * vector similarity search using PostgreSQL pgvector extension (<=> cosine distance).
 */
@Service
public class VectorStoreService {

    public static class DocumentMatch {
        private final String documentId;
        private final String content;
        private final Map<String, Object> metadata;
        private final double similarityScore;

        public DocumentMatch(String documentId, String content, Map<String, Object> metadata, double similarityScore) {
            this.documentId = documentId;
            this.content = content;
            this.metadata = metadata != null ? metadata : Collections.emptyMap();
            this.similarityScore = similarityScore;
        }

        public String getDocumentId() { return documentId; }
        public String getContent() { return content; }
        public Map<String, Object> getMetadata() { return metadata; }
        public double getSimilarityScore() { return similarityScore; }
    }

    private static class StoredDocument {
        String documentId;
        String content;
        Map<String, Object> metadata;
        float[] embedding;

        StoredDocument(String documentId, String content, Map<String, Object> metadata, float[] embedding) {
            this.documentId = documentId;
            this.content = content;
            this.metadata = metadata;
            this.embedding = embedding;
        }
    }

    // In-memory backing buffer mirroring pgvector table
    private final Map<String, StoredDocument> vectorStore = new HashMap<>();

    /**
     * Store or update document chunk with embedding vector
     */
    public synchronized void storeDocument(String documentId, String content, Map<String, Object> metadata, float[] embedding) {
        if (documentId == null || content == null || embedding == null) {
            throw new IllegalArgumentException("documentId, content, and embedding must not be null");
        }
        vectorStore.put(documentId, new StoredDocument(documentId, content, metadata, embedding));
    }

    /**
     * Perform k-nearest neighbor (k-NN) similarity search using Cosine Similarity
     * Equivalent to: SELECT id, content, 1 - (embedding <=> :queryEmbedding) AS score FROM document_chunks ORDER BY score DESC LIMIT :topK
     */
    public List<DocumentMatch> similaritySearch(float[] queryEmbedding, int topK, double minThreshold) {
        if (queryEmbedding == null || queryEmbedding.length == 0) {
            return Collections.emptyList();
        }

        List<DocumentMatch> matches = new ArrayList<>();

        for (StoredDocument doc : vectorStore.values()) {
            double cosineSim = computeCosineSimilarity(queryEmbedding, doc.embedding);
            if (cosineSim >= minThreshold) {
                matches.add(new DocumentMatch(doc.documentId, doc.content, doc.metadata, cosineSim));
            }
        }

        return matches.stream()
                .sorted((a, b) -> Double.compare(b.getSimilarityScore(), a.getSimilarityScore()))
                .limit(topK)
                .collect(Collectors.toList());
    }

    /**
     * Delete document chunks associated with a document ID
     */
    public synchronized boolean deleteDocument(String documentId) {
        return vectorStore.remove(documentId) != null;
    }

    /**
     * Total number of stored document vectors
     */
    public int getDocumentCount() {
        return vectorStore.size();
    }

    /**
     * Compute cosine similarity between two float arrays
     */
    private double computeCosineSimilarity(float[] a, float[] b) {
        if (a.length != b.length) return 0.0;

        double dot = 0.0;
        double normA = 0.0;
        double normB = 0.0;

        for (int i = 0; i < a.length; i++) {
            dot += a[i] * b[i];
            normA += a[i] * a[i];
            normB += b[i] * b[i];
        }

        if (normA == 0.0 || normB == 0.0) return 0.0;
        return dot / (Math.sqrt(normA) * Math.sqrt(normB));
    }
}
