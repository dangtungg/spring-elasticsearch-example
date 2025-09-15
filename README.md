# Spring Boot Elasticsearch Integration Example

A comprehensive Spring Boot application that demonstrates all essential Elasticsearch features. This example includes:

## Key Features Covered:

- **CRUD Operations** - Create, read, update, delete products
- **Full-text Search** - Search across name, description, category, brand
- **Advanced Filtering** - Price ranges, categories, ratings, stock status
- **Pagination & Sorting** - Efficient handling of large result sets
- **Aggregations** - Category counts, brand statistics, price ranges
- **Autocomplete/Suggestions** - Type-ahead functionality
- **Bulk Operations** - Efficiently handle multiple documents
- **Custom Queries** - Both repository methods and native Elasticsearch queries

## Running the Application

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

## API Examples

- **Basic CRUD Operations**

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
```

- **Search Operations**

```shell
# Basic search
curl "http://localhost:8080/api/products/search?query=Apple&page=0&size=5"

# Advanced search with filters
curl "http://localhost:8080/api/products/search/advanced?query=laptop&category=Laptops&minPrice=1000&maxPrice=2000&sortBy=price&sortDir=asc"

# Get suggestions
curl "http://localhost:8080/api/products/suggestions?input=App"

# Get aggregations
curl "http://localhost:8080/api/products/aggregations"
```

- **Category and Filter Operations**

```shell
# Products by category
curl "http://localhost:8080/api/products/category/Laptops"

# Featured products
curl "http://localhost:8080/api/products/featured"

# High-rated products
curl "http://localhost:8080/api/products/high-rated?minRating=4.5"

# Price range
curl "http://localhost:8080/api/products/price-range?minPrice=200&maxPrice=500"
```

## Kibana Usage

Access Kibana at [http://localhost:5601](http://localhost:5601) to:

- Create visualizations and dashboards
- Explore your product data
- Monitor Elasticsearch cluster health
- Create index patterns for the products index