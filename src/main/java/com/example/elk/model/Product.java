package com.example.elk.model;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.Setter;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import org.springframework.data.elasticsearch.annotations.Setting;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.List;

@Setter
@Getter
@Document(indexName = "products")
@Setting(settingPath = "elasticsearch-settings.json")
public class Product {

    @Id
    private String id;

    @Field(type = FieldType.Text, analyzer = "standard")
    @NotBlank(message = "Product name is required")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    @NotBlank(message = "Category is required")
    private String category;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Double)
    @DecimalMin(value = "0.0", inclusive = false, message = "Price must be greater than 0")
    private BigDecimal price;

    @Field(type = FieldType.Integer)
    @Min(value = 0, message = "Stock quantity cannot be negative")
    private Integer stockQuantity;

    @Field(type = FieldType.Double)
    @DecimalMin(value = "0.0", message = "Rating must be between 0 and 5")
    @DecimalMax(value = "5.0", message = "Rating must be between 0 and 5")
    private Double rating;

    @Field(type = FieldType.Integer)
    private Integer reviewCount;

    @Field(type = FieldType.Keyword)
    private List<String> tags;

    @Field(type = FieldType.Boolean)
    private Boolean active = true;

    @Field(type = FieldType.Boolean)
    private Boolean featured = false;

    @Field(type = FieldType.Long)
    private Long createdAt;

    @Field(type = FieldType.Long)
    private Long updatedAt;

    public Product() {
        LocalDateTime now = LocalDateTime.now().withNano(LocalDateTime.now().getNano() / 1000000 * 1000000);
        long epochMilli = now.toInstant(ZoneOffset.UTC).toEpochMilli();
        this.createdAt = epochMilli;
        this.updatedAt = epochMilli;
    }

    public Product(String name, String description, String category, String brand,
                   BigDecimal price, Integer stockQuantity) {
        this();
        this.name = name;
        this.description = description;
        this.category = category;
        this.brand = brand;
        this.price = price;
        this.stockQuantity = stockQuantity;
        this.rating = 0.0;
        this.reviewCount = 0;
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category='" + category + '\'' +
                ", brand='" + brand + '\'' +
                ", price=" + price +
                ", active=" + active +
                '}';
    }
}
