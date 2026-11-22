package com.enterprise.rag.controller;

import com.enterprise.rag.model.DocumentRecord;
import com.enterprise.rag.model.RagQueryRequest;
import com.enterprise.rag.model.RagQueryResponse;
import com.enterprise.rag.service.RagService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/rag")
@CrossOrigin(origins = "*")
public class RagController {

    private final RagService ragService;

    public RagController(RagService ragService) {
        this.ragService = ragService;
    }

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "healthy",
                "service", "Java 21 LangChain4j RAG Assistant",
                "framework", "Spring Boot 3.3.0",
                "vectorStore", "pgvector HNSW"
        ));
    }

    @GetMapping("/stats")
    public ResponseEntity<Map<String, Object>> getStats() {
        return ResponseEntity.ok(ragService.getRagStats());
    }

    @GetMapping("/documents")
    public ResponseEntity<List<DocumentRecord>> getDocuments() {
        return ResponseEntity.ok(ragService.getDocuments());
    }

    @PostMapping("/query")
    public ResponseEntity<RagQueryResponse> queryRag(@RequestBody RagQueryRequest request) {
        return ResponseEntity.ok(ragService.executeRagQuery(request));
    }
}
