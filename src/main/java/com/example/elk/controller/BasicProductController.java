package com.example.elk.controller;

import com.example.elk.model.Product;
import com.example.elk.service.BasicProductService;
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

/**
 * Basic Product Controller - Handles simple CRUD operations and repository-based queries
 * <p>
 * API Endpoints:
 * ==============
 * <p>
 * CRUD Operations:
 * - POST   /api/products                    - Create product
 * - GET    /api/products                    - Get all products
 * - GET    /api/products/{id}               - Get product by ID
 * - PUT    /api/products/{id}               - Update product
 * - DELETE /api/products/{id}               - Delete product
 * - POST   /api/products/bulk               - Create multiple products
 * - DELETE /api/products/bulk               - Delete all products
 * <p>
 * Simple Filtering:
 * - GET    /api/products/category/{category}              - Products by category
 * - GET    /api/products/category/{category}/page         - Products by category (paginated)
 * - GET    /api/products/brand/{brand}                    - Products by brand
 * - GET    /api/products/featured                         - Featured products
 * - GET    /api/products/high-rated                       - High-rated products
 * - GET    /api/products/in-stock                         - In-stock products
 * - GET    /api/products/price-range                      - Products in price range
 * - GET    /api/products/price-range/page                 - Products in price range (paginated)
 * - GET    /api/products/category/{category}/price-range  - Category + price filter
 * - GET    /api/products/tags                             - Products by tags
 * - GET    /api/products/text-search                      - Simple text search
 * - GET    /api/products/featured/price-range             - Featured products in price range
 */
@Validated
@RestController
@RequiredArgsConstructor
@CrossOrigin(origins = "*")
@RequestMapping("/api/products")
public class BasicProductController {

    private final BasicProductService basicProductService;

    // ============================================================================
    // CRUD OPERATIONS
    // ============================================================================

    @PostMapping
    public ResponseEntity<Product> createProduct(@Valid @RequestBody Product product) {
        Product savedProduct = basicProductService.save(product);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProduct);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Product> getProduct(@PathVariable String id) {
        Optional<Product> product = basicProductService.findById(id);
        return product.map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }

    @PutMapping("/{id}")
    public ResponseEntity<Product> updateProduct(@PathVariable String id,
                                                 @Valid @RequestBody Product product) {
        Optional<Product> existingProduct = basicProductService.findById(id);
        if (existingProduct.isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        product.setId(id);
        product.setCreatedAt(existingProduct.get().getCreatedAt());
        Product updatedProduct = basicProductService.save(product);
        return ResponseEntity.ok(updatedProduct);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteProduct(@PathVariable String id) {
        if (basicProductService.findById(id).isEmpty()) {
            return ResponseEntity.notFound().build();
        }

        basicProductService.deleteById(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping
    public ResponseEntity<List<Product>> getAllProducts() {
        List<Product> products = basicProductService.findAll();
        return ResponseEntity.ok(products);
    }

    // ============================================================================
    // BULK OPERATIONS
    // ============================================================================

    @PostMapping("/bulk")
    public ResponseEntity<List<Product>> createProducts(@Valid @RequestBody List<Product> products) {
        List<Product> savedProducts = basicProductService.saveAll(products);
        return ResponseEntity.status(HttpStatus.CREATED).body(savedProducts);
    }

    @DeleteMapping("/bulk")
    public ResponseEntity<Void> deleteAllProducts() {
        basicProductService.deleteAll();
        return ResponseEntity.noContent().build();
    }

    // ============================================================================
    // SIMPLE FILTERING OPERATIONS
    // ============================================================================

    @GetMapping("/category/{category}")
    public ResponseEntity<List<Product>> getProductsByCategory(@PathVariable String category) {
        List<Product> products = basicProductService.findByCategory(category);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}/page")
    public ResponseEntity<Page<Product>> getProductsByCategoryPaged(
            @PathVariable String category,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Page<Product> products = basicProductService.findByCategory(category, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/brand/{brand}")
    public ResponseEntity<List<Product>> getProductsByBrand(@PathVariable String brand) {
        List<Product> products = basicProductService.findByBrand(brand);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured")
    public ResponseEntity<List<Product>> getFeaturedProducts() {
        List<Product> products = basicProductService.findFeaturedProducts();
        return ResponseEntity.ok(products);
    }

    @GetMapping("/high-rated")
    public ResponseEntity<List<Product>> getHighRatedProducts(
            @RequestParam(defaultValue = "4.0") Double minRating) {

        List<Product> products = basicProductService.findHighRatedProducts(minRating);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/in-stock")
    public ResponseEntity<List<Product>> getInStockProducts(
            @RequestParam(defaultValue = "0") @Min(0) Integer minStock) {
        List<Product> products = basicProductService.findInStockProducts(minStock);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range")
    public ResponseEntity<List<Product>> getProductsByPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        List<Product> products = basicProductService.findByPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/price-range/page")
    public ResponseEntity<Page<Product>> getProductsByPriceRangePaged(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice,
            @RequestParam(defaultValue = "0") @Min(0) int page,
            @RequestParam(defaultValue = "10") @Min(1) int size) {

        Page<Product> products = basicProductService.findByPriceRange(minPrice, maxPrice, page, size);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/category/{category}/price-range")
    public ResponseEntity<List<Product>> getProductsByCategoryAndPriceRange(
            @PathVariable String category,
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {

        List<Product> products = basicProductService.findByCategoryAndPriceRange(category, minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/tags")
    public ResponseEntity<List<Product>> getProductsByTags(@RequestParam String... tags) {
        List<Product> products = basicProductService.findByTags(tags);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/text-search")
    public ResponseEntity<List<Product>> searchInNameOrDescription(@RequestParam String query) {
        List<Product> products = basicProductService.findByNameOrDescription(query);
        return ResponseEntity.ok(products);
    }

    @GetMapping("/featured/price-range")
    public ResponseEntity<List<Product>> getFeaturedProductsInPriceRange(
            @RequestParam BigDecimal minPrice,
            @RequestParam BigDecimal maxPrice) {
        List<Product> products = basicProductService.findFeaturedProductsInPriceRange(minPrice, maxPrice);
        return ResponseEntity.ok(products);
    }

    // ============================================================================
    // HEALTH CHECK
    // ============================================================================

    @GetMapping("/health")
    public ResponseEntity<Map<String, String>> healthCheck() {
        return ResponseEntity.ok(Map.of(
                "status", "UP",
                "service", "Basic Product Service",
                "timestamp", java.time.LocalDateTime.now().toString()
        ));
    }
}