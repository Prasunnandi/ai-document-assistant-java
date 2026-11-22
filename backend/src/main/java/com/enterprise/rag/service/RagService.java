package com.enterprise.rag.service;

import com.enterprise.rag.model.*;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
public class RagService {

    private final List<DocumentRecord> documents = new ArrayList<>();
    private final List<DocumentChunk> chunks = new ArrayList<>();

    public RagService() {
        initBenchmarkData();
    }

    private void initBenchmarkData() {
        documents.add(DocumentRecord.builder()
                .documentId("DOC-RAG-01")
                .title("Enterprise_Information_Security_Policy_2026.pdf")
                .category("Cybersecurity & Compliance")
                .format("PDF")
                .pageCount(64)
                .chunkCount(320)
                .embeddingModel("text-embedding-3-small")
                .vectorDim(1536)
                .indexType("HNSW-Cosine")
                .retrievalMrr(0.942)
                .status("INDEXED")
                .indexedAt(LocalDateTime.now().minusDays(3))
                .build());

        documents.add(DocumentRecord.builder()
                .documentId("DOC-RAG-02")
                .title("HIPAA_Business_Associate_Agreement_Standard.docx")
                .category("Legal & Regulatory")
                .format("DOCX")
                .pageCount(28)
                .chunkCount(142)
                .embeddingModel("text-embedding-3-small")
                .vectorDim(1536)
                .indexType("HNSW-Cosine")
                .retrievalMrr(0.918)
                .status("INDEXED")
                .indexedAt(LocalDateTime.now().minusDays(5))
                .build());

        documents.add(DocumentRecord.builder()
                .documentId("DOC-RAG-03")
                .title("Kubernetes_Zero_Trust_Architecture_Spec.md")
                .category("Infrastructure & DevOps")
                .format("Markdown")
                .pageCount(45)
                .chunkCount(215)
                .embeddingModel("bge-large-en-v1.5")
                .vectorDim(1024)
                .indexType("HNSW-Cosine")
                .retrievalMrr(0.965)
                .status("INDEXED")
                .indexedAt(LocalDateTime.now().minusDays(1))
                .build());

        documents.add(DocumentRecord.builder()
                .documentId("DOC-RAG-05")
                .title("SOC2_Type2_Control_Audit_Report_FY2025.pdf")
                .category("Compliance & Audit")
                .format("PDF")
                .pageCount(110)
                .chunkCount(540)
                .embeddingModel("text-embedding-3-small")
                .vectorDim(1536)
                .indexType("HNSW-Cosine")
                .retrievalMrr(0.951)
                .status("INDEXED")
                .indexedAt(LocalDateTime.now().minusDays(8))
                .build());

        // Chunks
        chunks.add(DocumentChunk.builder()
                .chunkId("CHK-1001")
                .documentId("DOC-RAG-01")
                .documentTitle("Enterprise_Information_Security_Policy_2026.pdf")
                .pageNumber(14)
                .chunkIndex(42)
                .content("Section 4.3: Mandatory Multi-Factor Authentication (MFA). All remote access to enterprise cloud environments and production clusters must enforce FIDO2 WebAuthn hardware tokens or TOTP authenticator apps. SMS-based 2FA is explicitly deprecated.")
                .similarityScore(0.94)
                .build());

        chunks.add(DocumentChunk.builder()
                .chunkId("CHK-1002")
                .documentId("DOC-RAG-01")
                .documentTitle("Enterprise_Information_Security_Policy_2026.pdf")
                .pageNumber(22)
                .chunkIndex(68)
                .content("Section 7.1: Data at Rest Encryption. All PostgreSQL volumes, S3 buckets, and persistent disk snapshots must enforce AES-256 or ChaCha20 encryption with automated annual KMS key rotation.")
                .similarityScore(0.89)
                .build());

        chunks.add(DocumentChunk.builder()
                .chunkId("CHK-1003")
                .documentId("DOC-RAG-03")
                .documentTitle("Kubernetes_Zero_Trust_Architecture_Spec.md")
                .pageNumber(6)
                .chunkIndex(18)
                .content("NetworkPolicy Specification: Default-deny all ingress and egress traffic between namespaces. Service-to-service communication must require mutual TLS (mTLS) with SPIFFE/SPIRE x509 cryptographic identities.")
                .similarityScore(0.92)
                .build());

        chunks.add(DocumentChunk.builder()
                .chunkId("CHK-1004")
                .documentId("DOC-RAG-05")
                .documentTitle("SOC2_Type2_Control_Audit_Report_FY2025.pdf")
                .pageNumber(48)
                .chunkIndex(112)
                .content("CC6.6 Incident Response Testing: Formal disaster recovery and failover drills are conducted bi-annually. RTO SLA is established at 2.0 hours with an RPO of 15 minutes across multi-region standby clusters.")
                .similarityScore(0.88)
                .build());
    }

    public List<DocumentRecord> getDocuments() {
        return documents;
    }

    public Map<String, Object> getRagStats() {
        int totalDocs = documents.size();
        int totalChunks = documents.stream().mapToInt(DocumentRecord::getChunkCount).sum();
        double avgMrr = documents.stream().mapToDouble(DocumentRecord::getRetrievalMrr).average().orElse(0.93);

        Map<String, Object> stats = new HashMap<>();
        stats.put("totalDocuments", totalDocs);
        stats.put("totalIndexedChunks", totalChunks);
        stats.put("averageRetrievalMrr", Math.round(avgMrr * 1000.0) / 1000.0);
        stats.put("vectorDimensions", 1536);
        stats.put("indexAlgorithm", "HNSW (M=16, efConstruction=64)");
        return stats;
    }

    public RagQueryResponse executeRagQuery(RagQueryRequest request) {
        long startTime = System.currentTimeMillis();
        String queryLower = request.getQuery() != null ? request.getQuery().toLowerCase() : "";

        List<DocumentChunk> matchingChunks;
        String synthesizedAnswer;

        if (queryLower.contains("mfa") || queryLower.contains("authentication") || queryLower.contains("token")) {
            matchingChunks = chunks.stream()
                    .filter(c -> c.getDocumentId().equals("DOC-RAG-01"))
                    .collect(Collectors.toList());
            synthesizedAnswer = "According to Section 4.3 of the **Enterprise Information Security Policy 2026** [Page 14], all remote access into enterprise environments mandates **FIDO2 WebAuthn hardware tokens** or TOTP authenticator applications. **SMS-based 2FA is strictly deprecated** due to SIM-swapping vulnerabilities.";
        } else if (queryLower.contains("encryption") || queryLower.contains("aes") || queryLower.contains("kms")) {
            matchingChunks = chunks.stream()
                    .filter(c -> c.getChunkId().equals("CHK-1002"))
                    .collect(Collectors.toList());
            synthesizedAnswer = "Under Section 7.1 of the **Security Policy** [Page 22], data at rest across PostgreSQL, object storage, and disk snapshots must be encrypted using **AES-256** with automated annual AWS KMS/GCP KMS key rotation.";
        } else if (queryLower.contains("kubernetes") || queryLower.contains("mtls") || queryLower.contains("zero trust")) {
            matchingChunks = chunks.stream()
                    .filter(c -> c.getDocumentId().equals("DOC-RAG-03"))
                    .collect(Collectors.toList());
            synthesizedAnswer = "The **Kubernetes Zero Trust Architecture Specification** [Page 6] mandates a **default-deny NetworkPolicy** for both ingress and egress. Microservices communicate strictly via **mutual TLS (mTLS)** validated with SPIFFE/SPIRE x509 workload identities.";
        } else if (queryLower.contains("rto") || queryLower.contains("rpo") || queryLower.contains("soc2") || queryLower.contains("disaster")) {
            matchingChunks = chunks.stream()
                    .filter(c -> c.getDocumentId().equals("DOC-RAG-05"))
                    .collect(Collectors.toList());
            synthesizedAnswer = "The **SOC2 Type 2 FY2025 Audit Report** [Control CC6.6, Page 48] verifies that disaster recovery drills are conducted bi-annually, with a certified **Recovery Time Objective (RTO) of 2.0 hours** and a **Recovery Point Objective (RPO) of 15 minutes**.";
        } else {
            matchingChunks = chunks.subList(0, Math.min(2, chunks.size()));
            synthesizedAnswer = "Based on indexed knowledge vectors across our enterprise policies, the system retrieved high-confidence semantic matches. Please review the attached ground-truth citations below.";
        }

        List<Citation> citations = matchingChunks.stream().map(c -> Citation.builder()
                .citationId("CIT-" + c.getChunkId())
                .documentId(c.getDocumentId())
                .documentTitle(c.getDocumentTitle())
                .pageNumber(c.getPageNumber())
                .excerpt(c.getContent().substring(0, Math.min(120, c.getContent().length())) + "...")
                .relevanceScore(c.getSimilarityScore())
                .build()).collect(Collectors.toList());

        long latency = System.currentTimeMillis() - startTime + 85; // simulate LLM & pgvector latency

        return RagQueryResponse.builder()
                .query(request.getQuery())
                .synthesizedAnswer(synthesizedAnswer)
                .responseConfidence(0.96)
                .latencyMs(latency)
                .retrievedChunksCount(matchingChunks.size())
                .citations(citations)
                .retrievedChunks(matchingChunks)
                .build();
    }
}
