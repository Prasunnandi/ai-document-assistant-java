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
public class RagQueryRequest {
    private String query;
    private String categoryFilter;
    private int topK;
    private double minSimilarityThreshold;
}
