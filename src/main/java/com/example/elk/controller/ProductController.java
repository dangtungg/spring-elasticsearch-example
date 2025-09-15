package com.example.elk.controller;

import com.example.elk.model.Product;
import com.example.elk.model.SearchResponse;
import com.example.elk.service.ProductService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Validated
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/products")
public class ProductController {

    private final ProductService productService;

    // CRUD Operations
    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = productService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        Optional<Product> product = productService.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id,
                                                 @Valid @RequestBody Product product) {
        Optional<Product> existingProduct = productService.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        product.setId(id);
        product.setCreatedAt(existingProduct.get().getCreatedAt());
        Product updatedProduct = productService.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (productService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        productService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = productService.findAll();
        return ResponseEntity.ok(products);
    }

    // Bulk Operations
    @PostMapping("/bulk")
    public ResponseEntity<List<Product>> createProducts(@Valid @RequestBody List<Product> products) {
        List<Product> savedProducts = productService.saveAll(products);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProducts);
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteAllProducts() {
        productService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    // Search Operations
    @GetMapping("/search")
    public ResponseEntity<SearchResponse<Product>> searchProducts(
            @RequestParam String query,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        SearchResponse<Product> response = productService.searchProducts(query, page, size);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/search/advanced")
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

        SearchResponse<Product> response = productService.advancedSearch(
                query, category, brand, minPrice, maxPrice, minRating, inStockOnly,
                page, size, sortBy, sortDir);

        return ResponseEntity.ok(response);
    }

    @GetMapping("/suggestions")
    public ResponseEntity<List<String>> getSuggestions(@RequestParam String input) {
        List<String> suggestions = productService.getSuggestions(input);
        return ResponseEntity.ok(suggestions);
    }

    // Category-based searches
    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = productService.findByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}/page")
    public ResponseEntity<Page<Product>> getProductsByCategoryPaged(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Page<Product> products = productService.findByCategory(category, page, size);
        return ResponseEntity.ok(products);
    }

    // Price-based searches
    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        List<Product> products = productService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    // Featured and high-rated products
    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        List<Product> products = productService.findFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/high-rated")
    public ResponseEntity<List<Product>> getHighRatedProducts(
            @RequestParam(defaultValue = "4.0") Double minRating) {

        List<Product> products = productService.findHighRatedProducts(minRating);
        return ResponseEntity.ok(products);
    }

    // Aggregations
    @GetMapping("/aggregations")
    public ResponseEntity<Map<String, Object>> getProductAggregations() {
        Map<String, Object> aggregations = productService.getProductAggregations();
        return ResponseEntity.ok(aggregations);
    }

    // Health check
    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Product Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}
