# Spring Boot Elasticsearch Integration Example

A comprehensive Spring Boot application that demonstrates all essential Elasticsearch features. This example includes:

## 🏗️ Architecture Overview

### Service Layer:

- **BasicProductService** - Simple CRUD and repository operations
- **AdvancedSearchService** - Complex Elasticsearch features

### Controller Layer:

- **BasicProductController** (`/api/products/*`) - Simple operations
- **AdvancedSearchController** (`/api/search/*`) - Complex search features

## 🚀 Elasticsearch Features Demonstrated:

### **Basic Operations (BasicProductService)**

- **CRUD Operations** - Create, read, update, delete products
- **Simple Filtering** - By category, brand, price, rating, stock status
- **Pagination & Sorting** - Efficient handling of large result sets
- **Bulk Operations** - Efficiently handle multiple documents
- **Repository Queries** - Spring Data method naming and custom @Query

### **Advanced Search (AdvancedSearchService)**

- **Full-text Search** - Multi-field search with relevance scoring
- **Boolean Queries** - Must, should, filter combinations
- **Query Boosting** - Relevance tuning for better results
- **Fuzzy Search** - Typo tolerance with edit distance
- **Aggregations** - Category counts, brand statistics, price ranges
- **Suggestions/Autocomplete** - Type-ahead functionality with prefix queries
- **Advanced Filtering** - Complex boolean logic with Criteria API
- **Custom Sorting** - Score-based and field-based sorting
- **Enhanced Repository Queries** - Readable text block syntax

---

## 🚀 Running the Application

- Start Elasticsearch and Kibana

```shell
# Start the services
docker compose up -d

# Check status
docker compose ps

# View logs
docker compose logs -f elasticsearch
```

- Run Spring Boot Application

```shell
# Using Maven
mvn spring-boot:run

# Or build and run JAR
mvn clean package
java -jar target/spring-elasticsearch-example-0.0.1.jar
```

## 📋 Basic Operations API (`/api/products/*`)

**Handled by BasicProductController & BasicProductService**

### **CRUD Operations**

```shell
# Get all products
curl "http://localhost:8080/api/products"

# Get product by ID
curl "http://localhost:8080/api/products/{id}"

# Create new product
curl -X POST "http://localhost:8080/api/products" \
  -H "Content-Type: application/json" \
  -d '{
    "name": "iPad Pro",
    "description": "Apple tablet with M2 chip",
    "category": "Tablets",
    "brand": "Apple",
    "price": 1099.00,
    "stockQuantity": 25,
    "tags": ["tablet", "apple", "professional"]
  }'

# Update product
curl -X PUT "http://localhost:8080/api/products/{id}" \
  -H "Content-Type: application/json" \
  -d '{...}'

# Delete product
curl -X DELETE "http://localhost:8080/api/products/{id}"

# Bulk operations
curl -X POST "http://localhost:8080/api/products/bulk" \
  -H "Content-Type: application/json" \
  -d '[{...}, {...}]'
```

### **Simple Filtering & Repository Operations**

```shell
# Products by category
curl "http://localhost:8080/api/products/category/Laptops"

# Products by brand
curl "http://localhost:8080/api/products/brand/Apple"

# Featured products
curl "http://localhost:8080/api/products/featured"

# High-rated products
curl "http://localhost:8080/api/products/high-rated?minRating=4.5"

# Price range
curl "http://localhost:8080/api/products/price-range?minPrice=200&maxPrice=500"

# In-stock products
curl "http://localhost:8080/api/products/in-stock?minStock=5"

# Simple text search (repository query)
curl "http://localhost:8080/api/products/text-search?query=gaming"
```

### **Enhanced Search Features**

```shell
# Enhanced keyword search with boosting
curl "http://localhost:8080/api/search/enhanced?keyword=laptop"

# Fuzzy search (handles typos like 'iPhon' → 'iPhone')
curl "http://localhost:8080/api/search/fuzzy?query=iPhon"

# Autocomplete suggestions
curl "http://localhost:8080/api/search/suggestions?input=App"

# Product aggregations and analytics
curl "http://localhost:8080/api/search/aggregations"
```

### **Elasticsearch Features Demonstrated**

| Feature                | Implementation            | Endpoint                   |
|------------------------|---------------------------|----------------------------|
| **Multi-field Search** | BoolQuery + MatchQuery    | `/api/search`              |
| **Query Boosting**     | MatchQuery.boost()        | `/api/search`              |
| **Complex Filtering**  | Criteria API              | `/api/search/advanced`     |
| **Fuzzy Search**       | FuzzyQuery with AUTO      | `/api/search/fuzzy`        |
| **Prefix Queries**     | PrefixQuery               | `/api/search/suggestions`  |
| **Aggregations**       | Terms, Histogram, Metrics | `/api/search/aggregations` |
| **Repository Queries** | Text block syntax         | Various                    |
| **Boolean Logic**      | Must/Should/Filter        | `/api/search/advanced`     |
| **Custom Sorting**     | Score & field-based       | `/api/search/advanced`     |

### **Complex Filtering & Pagination**

```shell
# Category with pagination
curl "http://localhost:8080/api/products/category/Laptops/page?page=0&size=5"

# Price range with pagination
curl "http://localhost:8080/api/products/price-range/page?minPrice=200&maxPrice=500&page=0&size=10"

# Category with price range filter
curl "http://localhost:8080/api/products/category/Laptops/price-range?minPrice=1000&maxPrice=2000"

# Products by multiple tags
curl "http://localhost:8080/api/products/tags?tags=gaming&tags=professional"

# Featured products in price range
curl "http://localhost:8080/api/products/featured/price-range?minPrice=500&maxPrice=1500"
```

---

## 🔍 Advanced Search API (`/api/search/*`)

**Handled by AdvancedSearchController & AdvancedSearchService**

### **Full-text Search with Elasticsearch Client**

```shell
# Basic full-text search with scoring
curl "http://localhost:8080/api/search?query=laptop&page=0&size=10"

# Advanced search with complex filtering
curl "http://localhost:8080/api/search/advanced?query=gaming&category=Laptops&minPrice=1000&maxPrice=2000&sortBy=price&sortDir=asc"
```

---

## 📊 Repository Layer Improvements

### **Readable Text Block Syntax**

Before (Hard to Read):

```java
@Query("{\"bool\": {\"should\": [{\"match\": {\"name\": \"?0\"}}]}}")
```

After (Clean & Readable):

```java
@Query("""
        {
          "bool": {
            "should": [
              {"match": {"name": "?0"}},
              {"match": {"description": "?0"}}
            ]
          }
        }
        """)
```

---

## 🧪 Kibana Usage

Access Kibana at [http://localhost:5601](http://localhost:5601) to:

- Create visualizations and dashboards
- Explore your product data
- Monitor Elasticsearch cluster health
- Create index patterns for the products index