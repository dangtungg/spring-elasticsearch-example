package com.example.elk.service;

import com.example.elk.model.Product;
import com.example.elk.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for BasicProductService
 * <p>
 * These tests verify the basic CRUD operations and simple filtering functionality
 * without requiring a running Elasticsearch instance.
 */
@ExtendWith(MockitoExtension.class)
class BasicProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private BasicProductService basicProductService;

    private Product sampleProduct;

    @BeforeEach
    void setUp() {
        sampleProduct = new Product();
        sampleProduct.setId("1");
        sampleProduct.setName("Test Product");
        sampleProduct.setDescription("Test Description");
        sampleProduct.setCategory("Electronics");
        sampleProduct.setBrand("TestBrand");
        sampleProduct.setPrice(BigDecimal.valueOf(99.99));
        sampleProduct.setStockQuantity(10);
        sampleProduct.setRating(4.5);
        sampleProduct.setActive(true);
        sampleProduct.setFeatured(false);
    }

    @Test
    void save_ShouldReturnSavedProduct() {
        // Given
        when(productRepository.save(any(Product.class))).thenReturn(sampleProduct);

        // When
        Product result = basicProductService.save(sampleProduct);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getName()).isEqualTo("Test Product");
        assertThat(result.getUpdatedAt()).isNotNull();
        verify(productRepository).save(sampleProduct);
    }

    @Test
    void findById_WhenProductExists_ShouldReturnProduct() {
        // Given
        when(productRepository.findById("1")).thenReturn(Optional.of(sampleProduct));

        // When
        Optional<Product> result = basicProductService.findById("1");

        // Then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("Test Product");
        verify(productRepository).findById("1");
    }

    @Test
    void findById_WhenProductDoesNotExist_ShouldReturnEmpty() {
        // Given
        when(productRepository.findById("999")).thenReturn(Optional.empty());

        // When
        Optional<Product> result = basicProductService.findById("999");

        // Then
        assertThat(result).isNotPresent();
        verify(productRepository).findById("999");
    }

    @Test
    void findByCategory_ShouldReturnProductsInCategory() {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.findByCategory("Electronics")).thenReturn(products);

        // When
        List<Product> result = basicProductService.findByCategory("Electronics");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCategory()).isEqualTo("Electronics");
        verify(productRepository).findByCategory("Electronics");
    }

    @Test
    void findByCategory_WithPagination_ShouldReturnPagedResults() {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(productRepository.findByCategory(eq("Electronics"), any(PageRequest.class))).thenReturn(page);

        // When
        Page<Product> result = basicProductService.findByCategory("Electronics", 0, 10);

        // Then
        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository).findByCategory(eq("Electronics"), any(PageRequest.class));
    }

    @Test
    void findByBrand_ShouldReturnProductsFromBrand() {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.findByBrand("TestBrand")).thenReturn(products);

        // When
        List<Product> result = basicProductService.findByBrand("TestBrand");

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getBrand()).isEqualTo("TestBrand");
        verify(productRepository).findByBrand("TestBrand");
    }

    @Test
    void findByPriceRange_ShouldReturnProductsInRange() {
        // Given
        BigDecimal minPrice = BigDecimal.valueOf(50.0);
        BigDecimal maxPrice = BigDecimal.valueOf(150.0);
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.findByPriceBetween(minPrice, maxPrice)).thenReturn(products);

        // When
        List<Product> result = basicProductService.findByPriceRange(minPrice, maxPrice);

        // Then
        assertThat(result).hasSize(1);
        verify(productRepository).findByPriceBetween(minPrice, maxPrice);
    }

    @Test
    void findFeaturedProducts_ShouldReturnOnlyFeaturedProducts() {
        // Given
        sampleProduct.setFeatured(true);
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.findByFeaturedTrue()).thenReturn(products);

        // When
        List<Product> result = basicProductService.findFeaturedProducts();

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFeatured()).isTrue();
        verify(productRepository).findByFeaturedTrue();
    }

    @Test
    void findHighRatedProducts_ShouldReturnHighRatedProducts() {
        // Given
        Double minRating = 4.0;
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.findByRatingGreaterThanEqual(minRating)).thenReturn(products);

        // When
        List<Product> result = basicProductService.findHighRatedProducts(minRating);

        // Then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getRating()).isGreaterThanOrEqualTo(minRating);
        verify(productRepository).findByRatingGreaterThanEqual(minRating);
    }

    @Test
    void deleteById_ShouldCallRepositoryDelete() {
        // When
        basicProductService.deleteById("1");

        // Then
        verify(productRepository).deleteById("1");
    }

    @Test
    void saveAll_ShouldSaveAllProductsAndUpdateTimestamps() {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(productRepository.saveAll(any())).thenReturn(products);

        // When
        List<Product> result = basicProductService.saveAll(products);

        // Then
        assertThat(result).hasSize(1);
        assertThat(sampleProduct.getUpdatedAt()).isNotNull();
        verify(productRepository).saveAll(products);
    }
}