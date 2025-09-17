package com.example.elk.service;

import com.example.elk.model.Product;
import com.example.elk.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

/**
 * Basic Product Service - Handles simple CRUD operations and repository-based queries
 * <p>
 * Features covered:
 * - Standard CRUD operations (Create, Read, Update, Delete)
 * - Simple filtering by single criteria (category, brand, price, rating, stock)
 * - Pagination support
 * - Bulk operations
 * - Basic repository query methods
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class BasicProductService {

    private final ProductRepository productRepository;

    // ============================================================================
    // BASIC CRUD OPERATIONS
    // ============================================================================

    /**
     * Save a product (create or update)
     */
    public Product save(Product product) {
        product.setUpdatedAt(Instant.now());
        log.debug("Saving product: {}", product.getName());
        return productRepository.save(product);
    }

    /**
     * Find product by ID
     */
    public Optional<Product> findById(String id) {
        log.debug("Finding product by ID: {}", id);
        return productRepository.findById(id);
    }

    /**
     * Delete product by ID
     */
    public void deleteById(String id) {
        log.debug("Deleting product by ID: {}", id);
        productRepository.deleteById(id);
    }

    /**
     * Get all products
     */
    public List<Product> findAll() {
        log.debug("Finding all products");
        List<Product> products = new ArrayList<>();
        productRepository.findAll().forEach(products::add);
        return products;
    }

    // ============================================================================
    // BULK OPERATIONS
    // ============================================================================

    /**
     * Save multiple products
     */
    public List<Product> saveAll(List<Product> products) {
        Instant now = Instant.now();
        products.forEach(product -> product.setUpdatedAt(now));
        log.debug("Saving {} products", products.size());
        return (List<Product>) productRepository.saveAll(products);
    }

    /**
     * Delete all products
     */
    public void deleteAll() {
        log.warn("Deleting all products");
        productRepository.deleteAll();
    }

    // ============================================================================
    // SIMPLE FILTERING OPERATIONS
    // ============================================================================

    /**
     * Find products by category
     */
    public List<Product> findByCategory(String category) {
        log.debug("Finding products by category: {}", category);
        return productRepository.findByCategory(category);
    }

    /**
     * Find products by category with pagination
     */
    public Page<Product> findByCategory(String category, int page, int size) {
        log.debug("Finding products by category: {} (page: {}, size: {})", category, page, size);
        return productRepository.findByCategory(category, PageRequest.of(page, size));
    }

    /**
     * Find products by brand
     */
    public List<Product> findByBrand(String brand) {
        log.debug("Finding products by brand: {}", brand);
        return productRepository.findByBrand(brand);
    }

    /**
     * Find products in price range
     */
    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Finding products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    /**
     * Find products in price range with pagination
     */
    public Page<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice, int page, int size) {
        log.debug("Finding products in price range: {} - {} (page: {}, size: {})",
                minPrice, maxPrice, page, size);
        return productRepository.findByPriceBetween(minPrice, maxPrice, PageRequest.of(page, size));
    }

    /**
     * Find featured products
     */
    public List<Product> findFeaturedProducts() {
        log.debug("Finding featured products");
        return productRepository.findByFeaturedTrue();
    }

    /**
     * Find high-rated products
     */
    public List<Product> findHighRatedProducts(Double minRating) {
        log.debug("Finding products with rating >= {}", minRating);
        return productRepository.findByRatingGreaterThanEqual(minRating);
    }

    /**
     * Find in-stock products
     */
    public List<Product> findInStockProducts(Integer minStock) {
        log.debug("Finding products with stock > {}", minStock);
        return productRepository.findByStockQuantityGreaterThan(minStock);
    }

    /**
     * Find active products
     */
    public List<Product> findActiveProducts() {
        log.debug("Finding active products");
        return productRepository.findByActiveTrue();
    }

    // ============================================================================
    // SIMPLE REPOSITORY-BASED SEARCH OPERATIONS
    // ============================================================================

    /**
     * Search in name or description using repository query
     */
    public List<Product> findByNameOrDescription(String searchTerm) {
        log.debug("Searching in name/description for: {}", searchTerm);
        return productRepository.findByNameOrDescription(searchTerm);
    }

    /**
     * Find products by category and price range
     */
    public List<Product> findByCategoryAndPriceRange(String category, BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Finding products by category: {} and price range: {} - {}", category, minPrice, maxPrice);
        return productRepository.findByCategoryAndPriceRange(category, minPrice, maxPrice);
    }

    /**
     * Find products by tags
     */
    public List<Product> findByTags(String... tags) {
        log.debug("Finding products by tags: {}", (Object) tags);
        return productRepository.findByTags(tags);
    }

    /**
     * Find featured products in price range
     */
    public List<Product> findFeaturedProductsInPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        log.debug("Finding featured products in price range: {} - {}", minPrice, maxPrice);
        return productRepository.findFeaturedProductsInPriceRange(minPrice, maxPrice);
    }
}