package com.enterprise.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class RagQueryResponse {
    private String query;
    private String synthesizedAnswer;
    private double responseConfidence;
    private long latencyMs;
    private int retrievedChunksCount;
    private List<Citation> citations;
    private List<DocumentChunk> retrievedChunks;
}
