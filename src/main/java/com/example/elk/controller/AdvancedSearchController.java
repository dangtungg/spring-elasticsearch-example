package com.example.elk.controller;

import com.example.elk.model.Product;
import com.example.elk.model.SearchResponse;
import com.example.elk.service.AdvancedSearchService;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

/**
 * Advanced Search Controller - Handles complex Elasticsearch search operations
 */
@Validated
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/search")
public class AdvancedSearchController {

    private final AdvancedSearchService advancedSearchService;

    // ============================================================================
    // FULL-TEXT SEARCH WITH ELASTICSEARCH CLIENT
    // ============================================================================

    /**
     * Basic full-text search across multiple fields with relevance scoring
     * <p>
     * Features: Multi-field search, boosting, score-based sorting
     * <p>
     * Example: GET /api/search?query=laptop&page=0&size=10
     */
    @GetMapping
    public ResponseEntity<SearchResponse<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        SearchResponse<Product> response = advancedSearchService.searchProducts(query, page, size);
        return ResponseEntity.ok(response);
    }

    /**
     * Advanced search with complex filtering using Criteria API
     * <p>
     * Features: Boolean logic, range queries, term filters, custom sorting
     * <p>
     * Example: GET /api/search/advanced?query=gaming&category=Laptops&minPrice=1000&maxPrice=2000&sortBy=price&sortDir=asc
     */
    @GetMapping("/advanced")
    public ResponseEntity<SearchResponse<Product>> advancedSearch(
            @RequestParam(required = false) String query,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) String brand,
            @RequestParam(required = false) BigDecimal minPrice,
            @RequestParam(required = false) BigDecimal maxPrice,
            @RequestParam(required = false) Double minRating,
            @RequestParam(required = false) Boolean inStockOnly,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size,
            @RequestParam(defaultValue = "score") String sortBy,
            @RequestParam(defaultValue = "desc") String sortDir) {

        SearchResponse<Product> response = advancedSearchService.advancedSearch(
                query, category, brand, minPrice, maxPrice, minRating, inStockOnly,
                page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    // ============================================================================
    // ENHANCED SEARCH OPERATIONS
    // ============================================================================

    /**
     * Enhanced keyword search with boosting using repository queries
     * <p>
     * Features: Repository @Query with boost, active filtering, minimum should match
     * <p>
     * Example: GET /api/search/enhanced?keyword=apple
     */
    @GetMapping("/enhanced")
    public ResponseEntity<List<Product>> searchActiveProductsByKeyword(@RequestParam String keyword) {
        List<Product> products = advancedSearchService.findActiveProductsByKeyword(keyword);
        return ResponseEntity.ok(products);
    }

    /**
     * Fuzzy search for typo tolerance
     * <p>
     * Features: Fuzzy queries with AUTO fuzziness, edit distance matching
     * <p>
     * Example: GET /api/search/fuzzy?query=iPhon (will find "iPhone")
     */
    @GetMapping("/fuzzy")
    public ResponseEntity<List<Product>> fuzzySearch(@RequestParam String query) {
        List<Product> products = advancedSearchService.findProductsWithFuzzySearch(query);
        return ResponseEntity.ok(products);
    }

    /**
     * Autocomplete suggestions for search input
     * <p>
     * Features: Prefix queries, source filtering, deduplication
     * <p>
     * Example: GET /api/search/suggestions?input=App
     */
    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String input) {
        List<String> suggestions = advancedSearchService.getSuggestions(input);
        return ResponseEntity.ok(suggestions);
    }

    // ============================================================================
    // ANALYTICS & AGGREGATIONS
    // ============================================================================

    /**
     * Product aggregations for analytics and faceted search
     * <p>
     * Features: Terms aggregations, metric aggregations, result processing
     * <p>
     * Response includes:
     * - categories: [{\"key\": \"Laptops\", \"count\": 15}, ...]
     * - brands: [{\"key\": \"Apple\", \"count\": 8}, ...]
     * - avgRating: 4.2
     * - totalProducts: 1250
     * - executionTimeMs: 45
     * <p>
     * Example: GET /api/search/aggregations
     */
    @GetMapping("/aggregations")
    public ResponseEntity<Map<String, Object>> getProductAggregations() {
        Map<String, Object> aggregations = advancedSearchService.getProductAggregations();
        return ResponseEntity.ok(aggregations);
    }

    // ============================================================================
    // HEALTH CHECK
    // ============================================================================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Advanced Search Service",
                "features", "Full-text search, Fuzzy search, Aggregations, Suggestions",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}