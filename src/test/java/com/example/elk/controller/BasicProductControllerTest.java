package com.example.elk.controller;

import com.example.elk.model.Product;
import com.example.elk.service.BasicProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

/**
 * Unit tests for BasicProductController
 * <p>
 * These tests verify the REST API endpoints for basic CRUD operations
 * without requiring a running Elasticsearch instance.
 */
@WebMvcTest(BasicProductController.class)
class BasicProductControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private BasicProductService basicProductService;

    @Autowired
    private ObjectMapper objectMapper;

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
    void createProduct_ShouldReturnCreatedProduct() throws Exception {
        // Given
        when(basicProductService.save(any(Product.class))).thenReturn(sampleProduct);

        // When & Then
        mockMvc.perform(post("/api/products")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Test Product"))
                .andExpect(jsonPath("$.category").value("Electronics"))
                .andExpect(jsonPath("$.price").value(99.99));
    }

    @Test
    void getProduct_WhenExists_ShouldReturnProduct() throws Exception {
        // Given
        when(basicProductService.findById("1")).thenReturn(Optional.of(sampleProduct));

        // When & Then
        mockMvc.perform(get("/api/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("1"))
                .andExpect(jsonPath("$.name").value("Test Product"));
    }

    @Test
    void getProduct_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(basicProductService.findById("999")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(get("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void getAllProducts_ShouldReturnProductList() throws Exception {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(basicProductService.findAll()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].name").value("Test Product"));
    }

    @Test
    void getProductsByCategory_ShouldReturnFilteredProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(basicProductService.findByCategory("Electronics")).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/category/Electronics"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].category").value("Electronics"));
    }

    @Test
    void getProductsByCategoryPaged_ShouldReturnPagedResults() throws Exception {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        Page<Product> page = new PageImpl<>(products, PageRequest.of(0, 10), 1);
        when(basicProductService.findByCategory(eq("Electronics"), eq(0), eq(10))).thenReturn(page);

        // When & Then
        mockMvc.perform(get("/api/products/category/Electronics/page")
                        .param("page", "0")
                        .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content").isArray())
                .andExpect(jsonPath("$.totalElements").value(1));
    }

    @Test
    void getProductsByBrand_ShouldReturnBrandProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(basicProductService.findByBrand("TestBrand")).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/brand/TestBrand"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].brand").value("TestBrand"));
    }

    @Test
    void getFeaturedProducts_ShouldReturnFeaturedProducts() throws Exception {
        // Given
        sampleProduct.setFeatured(true);
        List<Product> products = Arrays.asList(sampleProduct);
        when(basicProductService.findFeaturedProducts()).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/featured"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].featured").value(true));
    }

    @Test
    void getHighRatedProducts_ShouldReturnHighRatedProducts() throws Exception {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        when(basicProductService.findHighRatedProducts(4.0)).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/high-rated")
                        .param("minRating", "4.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].rating").value(4.5));
    }

    @Test
    void getProductsByPriceRange_ShouldReturnProductsInRange() throws Exception {
        // Given
        List<Product> products = Arrays.asList(sampleProduct);
        BigDecimal minPrice = BigDecimal.valueOf(50.0);
        BigDecimal maxPrice = BigDecimal.valueOf(150.0);
        when(basicProductService.findByPriceRange(minPrice, maxPrice)).thenReturn(products);

        // When & Then
        mockMvc.perform(get("/api/products/price-range")
                        .param("minPrice", "50.0")
                        .param("maxPrice", "150.0"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$").isArray())
                .andExpect(jsonPath("$[0].price").value(99.99));
    }

    @Test
    void updateProduct_WhenExists_ShouldReturnUpdatedProduct() throws Exception {
        // Given
        when(basicProductService.findById("1")).thenReturn(Optional.of(sampleProduct));
        when(basicProductService.save(any(Product.class))).thenReturn(sampleProduct);

        sampleProduct.setName("Updated Product");

        // When & Then
        mockMvc.perform(put("/api/products/1")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Updated Product"));
    }

    @Test
    void updateProduct_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(basicProductService.findById("999")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(put("/api/products/999")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(sampleProduct)))
                .andExpect(status().isNotFound());
    }

    @Test
    void deleteProduct_WhenExists_ShouldReturn204() throws Exception {
        // Given
        when(basicProductService.findById("1")).thenReturn(Optional.of(sampleProduct));

        // When & Then
        mockMvc.perform(delete("/api/products/1"))
                .andExpect(status().isNoContent());
    }

    @Test
    void deleteProduct_WhenNotExists_ShouldReturn404() throws Exception {
        // Given
        when(basicProductService.findById("999")).thenReturn(Optional.empty());

        // When & Then
        mockMvc.perform(delete("/api/products/999"))
                .andExpect(status().isNotFound());
    }

    @Test
    void healthCheck_ShouldReturnHealthStatus() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/products/health"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("UP"))
                .andExpect(jsonPath("$.service").value("Basic Product Service"));
    }
}