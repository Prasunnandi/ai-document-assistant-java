package com.enterprise.rag.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Citation {
    private String citationId;
    private String documentId;
    private String documentTitle;
    private int pageNumber;
    private String excerpt;
    private double relevanceScore;
}
