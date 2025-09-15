package com.example.elk.repository;

import com.example.elk.model.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.annotations.Query;
import org.springframework.data.elasticsearch.repository.ElasticsearchRepository;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

@Repository
public interface ProductRepository extends ElasticsearchRepository<Product, String> {

    // Simple query methods (Spring Data will generate implementation)
    List<Product> findByCategory(String category);
    List<Product> findByBrand(String brand);
    List<Product> findByActiveTrue();
    List<Product> findByFeaturedTrue();

    // Query with pagination
    Page<Product> findByCategory(String category, Pageable pageable);

    // Price range queries
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);
    Page<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice, Pageable pageable);

    // Full-text search on name and description
    @Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}, {\"match\": {\"description\": \"?0\"}}]}}")
    List<Product> findByNameOrDescription(String searchTerm);

    // Complex search with multiple conditions
    @Query("{\"bool\": {\"must\": [{\"term\": {\"category.keyword\": \"?0\"}}, {\"range\": {\"price\": {\"gte\": ?1, \"lte\": ?2}}}]}}")
    List<Product> findByCategoryAndPriceRange(String category, BigDecimal minPrice, BigDecimal maxPrice);

    // Search by tags
    @Query("{\"terms\": {\"tags.keyword\": [?0]}}")
    List<Product> findByTags(String... tags);

    // High-rated products
    List<Product> findByRatingGreaterThanEqual(Double minRating);

    // In stock products
    List<Product> findByStockQuantityGreaterThan(Integer minStock);
}
