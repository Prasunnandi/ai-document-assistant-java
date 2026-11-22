package com.enterprise.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DocumentRecord {
    private String documentId;
    private String title;
    private String category;
    private String format;
    private int pageCount;
    private int chunkCount;
    private String embeddingModel;
    private int vectorDim;
    private String indexType;
    private double retrievalMrr;
    private String status;
    private LocalDateTime indexedAt;
}
