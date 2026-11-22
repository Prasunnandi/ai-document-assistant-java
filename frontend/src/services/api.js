// AI Document Assistant Java RAG Service

const API_BASE = 'http://localhost:8085';

export const fallbackDocuments = [
  {
    documentId: "DOC-RAG-01",
    title: "Enterprise_Information_Security_Policy_2026.pdf",
    category: "Cybersecurity & Compliance",
    format: "PDF",
    pageCount: 64,
    chunkCount: 320,
    embeddingModel: "text-embedding-3-small",
    vectorDim: 1536,
    indexType: "HNSW-Cosine",
    retrievalMrr: 0.942,
    status: "INDEXED",
    indexedAt: "2026-03-26T14:20:00"
  },
  {
    documentId: "DOC-RAG-02",
    title: "HIPAA_Business_Associate_Agreement_Standard.docx",
    category: "Legal & Regulatory",
    format: "DOCX",
    pageCount: 28,
    chunkCount: 142,
    embeddingModel: "text-embedding-3-small",
    vectorDim: 1536,
    indexType: "HNSW-Cosine",
    retrievalMrr: 0.918,
    status: "INDEXED",
    indexedAt: "2026-03-24T11:10:00"
  },
  {
    documentId: "DOC-RAG-03",
    title: "Kubernetes_Zero_Trust_Architecture_Spec.md",
    category: "Infrastructure & DevOps",
    format: "Markdown",
    pageCount: 45,
    chunkCount: 215,
    embeddingModel: "bge-large-en-v1.5",
    vectorDim: 1024,
    indexType: "HNSW-Cosine",
    retrievalMrr: 0.965,
    status: "INDEXED",
    indexedAt: "2026-03-28T09:30:00"
  },
  {
    documentId: "DOC-RAG-05",
    title: "SOC2_Type2_Control_Audit_Report_FY2025.pdf",
    category: "Compliance & Audit",
    format: "PDF",
    pageCount: 110,
    chunkCount: 540,
    embeddingModel: "text-embedding-3-small",
    vectorDim: 1536,
    indexType: "HNSW-Cosine",
    retrievalMrr: 0.951,
    status: "INDEXED",
    indexedAt: "2026-03-21T16:45:00"
  }
];

export async function fetchDocuments() {
  try {
    const res = await fetch(`${API_BASE}/api/v1/rag/documents`);
    if (!res.ok) throw new Error('Network error');
    return await res.json();
  } catch (e) {
    return fallbackDocuments;
  }
}

export async function fetchStats() {
  try {
    const res = await fetch(`${API_BASE}/api/v1/rag/stats`);
    if (!res.ok) throw new Error('Network error');
    return await res.json();
  } catch (e) {
    return {
      totalDocuments: 4,
      totalIndexedChunks: 1217,
      averageRetrievalMrr: 0.944,
      vectorDimensions: 1536,
      indexAlgorithm: "HNSW (M=16, efConstruction=64)"
    };
  }
}

export async function queryRag(queryText) {
  try {
    const res = await fetch(`${API_BASE}/api/v1/rag/query`, {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ query: queryText, topK: 4, minSimilarityThreshold: 0.75 })
    });
    if (!res.ok) throw new Error('Query error');
    return await res.json();
  } catch (e) {
    const qLower = queryText.toLowerCase();
    if (qLower.includes('mfa') || qLower.includes('auth')) {
      return {
        query: queryText,
        synthesizedAnswer: "Section 4.3 of the **Enterprise Information Security Policy 2026** [Page 14] strictly requires **FIDO2 WebAuthn hardware tokens** or TOTP authenticator apps for all corporate access. **SMS-based 2FA is officially disallowed** across all production tiers.",
        responseConfidence: 0.97,
        latencyMs: 142,
        retrievedChunksCount: 2,
        citations: [
          {
            citationId: "CIT-1001",
            documentId: "DOC-RAG-01",
            documentTitle: "Enterprise_Information_Security_Policy_2026.pdf",
            pageNumber: 14,
            excerpt: "Mandatory Multi-Factor Authentication: All remote access must enforce FIDO2 WebAuthn hardware tokens...",
            relevanceScore: 0.94
          }
        ],
        retrievedChunks: [
          {
            chunkId: "CHK-1001",
            documentId: "DOC-RAG-01",
            documentTitle: "Enterprise_Information_Security_Policy_2026.pdf",
            pageNumber: 14,
            chunkIndex: 42,
            content: "Section 4.3: Mandatory Multi-Factor Authentication (MFA). All remote access to enterprise cloud environments and production clusters must enforce FIDO2 WebAuthn hardware tokens or TOTP authenticator apps. SMS-based 2FA is explicitly deprecated.",
            similarityScore: 0.94
          }
        ]
      };
    }
    return {
      query: queryText,
      synthesizedAnswer: "According to Control CC6.6 of the **SOC2 Type 2 Audit Report** [Page 48], disaster recovery drills verify an **RTO SLA of 2.0 hours** and an **RPO of 15 minutes** with automated cross-region database replication.",
      responseConfidence: 0.95,
      latencyMs: 128,
      retrievedChunksCount: 2,
      citations: [
        {
          citationId: "CIT-1004",
          documentId: "DOC-RAG-05",
          documentTitle: "SOC2_Type2_Control_Audit_Report_FY2025.pdf",
          pageNumber: 48,
          excerpt: "CC6.6 Incident Response Testing: Formal disaster recovery drills are conducted bi-annually with 2.0h RTO...",
          relevanceScore: 0.91
        }
      ],
      retrievedChunks: [
        {
          chunkId: "CHK-1004",
          documentId: "DOC-RAG-05",
          documentTitle: "SOC2_Type2_Control_Audit_Report_FY2025.pdf",
          pageNumber: 48,
          chunkIndex: 112,
          content: "CC6.6 Incident Response Testing: Formal disaster recovery and failover drills are conducted bi-annually. RTO SLA is established at 2.0 hours with an RPO of 15 minutes across multi-region standby clusters.",
          similarityScore: 0.91
        }
      ]
    };
  }
}
