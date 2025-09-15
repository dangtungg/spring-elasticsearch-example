package com.example.elk.model;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

@Getter
@Setter
@NoArgsConstructor
public class SearchResponse<T> {
    private List<T> content;
    private long totalElements;
    private int totalPages;
    private int currentPage;
    private int size;
    private Map<String, Object> aggregations;
    private long searchTimeMs;

    public SearchResponse(List<T> content, long totalElements, int currentPage, int size) {
        this.content = content;
        this.totalElements = totalElements;
        this.currentPage = currentPage;
        this.size = size;
        this.totalPages = (int) Math.ceil((double) totalElements / size);
    }
}
