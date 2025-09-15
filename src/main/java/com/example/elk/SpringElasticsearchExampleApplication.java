package com.example.elk;

import com.example.elk.model.Product;
import com.example.elk.service.ProductService;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.List;

@SpringBootApplication
@RequiredArgsConstructor
public class SpringElasticsearchExampleApplication implements CommandLineRunner {

    private final ProductService productService;

    public static void main(String[] args) {
        SpringApplication.run(SpringElasticsearchExampleApplication.class, args);
    }

    @Override
    public void run(String... args) throws Exception {
        // Initialize sample data
        initializeSampleData();
    }

    private void initializeSampleData() {
        // Check if data already exists
        if (!productService.findAll().isEmpty()) {
            System.out.println("Sample data already exists, skipping initialization...");
            return;
        }

        System.out.println("Initializing sample product data...");

        List<Product> sampleProducts = Arrays.asList(
                createProduct("MacBook Pro 16-inch", "Apple MacBook Pro with M2 chip", "Laptops", "Apple",
                        new BigDecimal("2399.00"), 50, 4.8, 120, Arrays.asList("laptop", "apple", "professional"), true, true),

                createProduct("Dell XPS 13", "Ultra-thin Dell laptop with Intel i7", "Laptops", "Dell",
                        new BigDecimal("1299.00"), 30, 4.5, 85, Arrays.asList("laptop", "dell", "ultrabook"), true, false),

                createProduct("iPhone 15 Pro", "Latest Apple iPhone with A17 chip", "Smartphones", "Apple",
                        new BigDecimal("999.00"), 100, 4.7, 200, Arrays.asList("smartphone", "apple", "5G"), true, true),

                createProduct("Samsung Galaxy S24", "Android smartphone with advanced camera", "Smartphones", "Samsung",
                        new BigDecimal("799.00"), 75, 4.4, 150, Arrays.asList("smartphone", "samsung", "android"), true, false),

                createProduct("Sony WH-1000XM5", "Noise-canceling wireless headphones", "Audio", "Sony",
                        new BigDecimal("399.00"), 200, 4.9, 300, Arrays.asList("headphones", "wireless", "noise-canceling"), true, true),

                createProduct("AirPods Pro", "Apple wireless earbuds with ANC", "Audio", "Apple",
                        new BigDecimal("249.00"), 150, 4.6, 180, Arrays.asList("earbuds", "apple", "wireless"), true, false),

                createProduct("LG OLED TV 55\"", "4K OLED Smart TV with HDR", "Electronics", "LG",
                        new BigDecimal("1499.00"), 25, 4.7, 95, Arrays.asList("tv", "oled", "4k", "smart"), true, false),

                createProduct("Nintendo Switch", "Hybrid gaming console", "Gaming", "Nintendo",
                        new BigDecimal("299.00"), 80, 4.8, 220, Arrays.asList("gaming", "console", "portable"), true, true)
        );

        try {
            productService.saveAll(sampleProducts);
            System.out.println("Sample data initialized successfully!");
        } catch (Exception e) {
            System.err.println("Failed to initialize sample data: " + e.getMessage());
        }
    }

    private Product createProduct(String name, String description, String category, String brand,
                                  BigDecimal price, Integer stock, Double rating, Integer reviewCount,
                                  List<String> tags, Boolean active, Boolean featured) {
        Product product = new Product(name, description, category, brand, price, stock);
        product.setRating(rating);
        product.setReviewCount(reviewCount);
        product.setTags(tags);
        product.setActive(active);
        product.setFeatured(featured);
        return product;
    }
}
