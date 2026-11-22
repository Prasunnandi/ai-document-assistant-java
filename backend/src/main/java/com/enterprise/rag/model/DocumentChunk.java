package com.enterprise.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentChunk {
    private String chunkId;
    private String documentId;
    private String documentTitle;
    private int pageNumber;
    private int chunkIndex;
    private String content;
    private double similarityScore;
}
