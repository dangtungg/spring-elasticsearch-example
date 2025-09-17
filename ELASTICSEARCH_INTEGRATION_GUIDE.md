# 🔍 Elasticsearch Integration Guide: Which Approach to Use When

## Overview

Spring Data Elasticsearch provides multiple ways to interact with Elasticsearch. This guide clarifies when and how to
use each approach, from simple to complex scenarios.

---

## 🏗️ **Integration Layers & Approaches**

### **Repository Layer** (`ProductRepository`)

- **JPA-style Query Methods** - Auto-generated queries
- **@Query Annotations** - Custom JSON queries

### **Service Layer**

- **Repository Injection** - Use repository methods
- **ElasticsearchOperations** - Direct Elasticsearch operations
    - **CriteriaQuery** - Type-safe query building
    - **NativeQuery** - Raw Elasticsearch queries with full control

---

## 📊 **Decision Matrix: Which Approach to Use**

| **Scenario**                   | **Approach**       | **Complexity** | **Performance** | **Flexibility** |
|--------------------------------|--------------------|----------------|-----------------|-----------------|
| Simple CRUD operations         | Repository Methods | ⭐              | ⭐⭐⭐             | ⭐               |
| Basic filtering (single field) | Repository Methods | ⭐              | ⭐⭐⭐             | ⭐               |
| Custom business logic          | Repository @Query  | ⭐⭐             | ⭐⭐⭐             | ⭐⭐              |
| Type-safe complex queries      | CriteriaQuery      | ⭐⭐⭐            | ⭐⭐              | ⭐⭐⭐             |
| Advanced ES features           | NativeQuery        | ⭐⭐⭐⭐           | ⭐⭐⭐⭐            | ⭐⭐⭐⭐            |

---

## 🎯 **1. Repository Query Methods**

**When to Use**: Simple, straightforward operations

### ✅ **Perfect for:**

- CRUD operations
- Single-field filtering
- Basic range queries
- Boolean field queries
- Simple pagination

### 📝 **Examples from Codebase:**

```java
// ProductRepository.java - Auto-generated queries
List<Product> findByCategory(String category);

List<Product> findByBrand(String brand);

List<Product> findByActiveTrue();

List<Product> findByPriceBetween(BigDecimal min, BigDecimal max);

Page<Product> findByCategory(String category, Pageable pageable);

// Usage in BasicProductService.java
public List<Product> findByCategory(String category) {
    return productRepository.findByCategory(category);
}
```

### 🔄 **Spring Data Magic:**

Spring automatically generates the Elasticsearch query based on method name:

- `findByCategory` → `{"term": {"category.keyword": "value"}}`
- `findByPriceBetween` → `{"range": {"price": {"gte": min, "lte": max}}}`

---

## 🎯 **2. Repository @Query Annotations**

**When to Use**: Custom logic that method names can't express

### ✅ **Perfect for:**

- Multi-field searches
- Custom JSON queries
- Query boosting
- Complex boolean logic
- Fuzzy matching

### 📝 **Examples from Codebase:**

```java
// ProductRepository.java - Custom queries
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
List<Product> findByNameOrDescription(String searchTerm);

@Query("""
        {
          "bool": {
            "must": [
              {"term": {"active": true}}
            ],
            "should": [
              {"match": {"name": {"query": "?0", "boost": 2.0}}},
              {"match": {"description": "?0"}}
            ],
            "minimum_should_match": 1
          }
        }
        """)
List<Product> findActiveProductsByKeyword(String keyword);
```

### 🔍 **Why Use This:**

- **Readability**: Text block syntax makes complex queries readable
- **Reusability**: Defined once, used multiple times
- **Performance**: Compiled once, executed many times
- **Type Safety**: Parameters are type-checked

---

## 🎯 **3. CriteriaQuery (ElasticsearchOperations)**

**When to Use**: Dynamic queries with type safety

### ✅ **Perfect for:**

- Dynamic filter combinations
- Conditional query building
- Type-safe query construction
- Spring Data integration
- Complex sorting logic

### 📝 **Example from Codebase:**

```java
// AdvancedSearchService.java - Dynamic query building
public SearchResponse<Product> advancedSearch(String query, String category,
                                              String brand, BigDecimal minPrice,
                                              BigDecimal maxPrice, Double minRating) {
    // Build criteria dynamically
    Criteria criteria = new Criteria("active").is(true);

    // Add conditions dynamically based on parameters
    if (query != null && !query.trim().isEmpty()) {
        Criteria textCriteria = new Criteria("name").matches(query)
                .or(new Criteria("description").matches(query));
        criteria = criteria.and(textCriteria);
    }

    if (category != null && !category.isEmpty()) {
        criteria = criteria.and(new Criteria("category.keyword").is(category));
    }

    if (minPrice != null && maxPrice != null) {
        criteria = criteria.and(new Criteria("price").between(minPrice, maxPrice));
    }

    // Build and execute query
    CriteriaQuery criteriaQuery = new CriteriaQuery(criteria)
            .setPageable(PageRequest.of(page, size));

    SearchHits<Product> hits = elasticsearchOperations.search(criteriaQuery, Product.class);
}
```

### 🎯 **Advantages:**

- **Type Safety**: Compile-time checking
- **Dynamic**: Build queries programmatically
- **Readable**: Java code instead of JSON
- **IDE Support**: Auto-completion and refactoring

---

## 🎯 **4. NativeQuery (ElasticsearchOperations)**

**When to Use**: Maximum control and advanced Elasticsearch features

### ✅ **Perfect for:**

- Advanced Elasticsearch features
- Custom scoring
- Aggregations
- Source filtering
- Complex query combinations
- Performance-critical operations

### 📝 **Examples from Codebase:**

```java
// AdvancedSearchService.java - Full-text search with boosting
public SearchResponse<Product> searchProducts(String query, int page, int size) {
    // Build complex boolean query with boosting
    Function<BoolQuery.Builder, ObjectBuilder<BoolQuery>> boolQuery = b -> b
            .should(MatchQuery.of(m -> m
                    .field("name")
                    .query(query)
                    .boost(2.0f))._toQuery())  // Name field boosted
            .should(MatchQuery.of(m -> m
                    .field("description")
                    .query(query))._toQuery())
            .minimumShouldMatch("1");

    Query esQuery = BoolQuery.of(boolQuery)._toQuery();

    // Build native query with sorting and pagination
    NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(esQuery)
            .withPageable(PageRequest.of(page, size))
            .withSort(SortOptions.of(s -> s.score(sc -> sc.order(SortOrder.Desc))))
            .build();

    SearchHits<Product> hits = elasticsearchOperations.search(searchQuery, Product.class);
}

// Suggestions with source filtering
public List<String> getSuggestions(String input) {
    Function<BoolQuery.Builder, ObjectBuilder<BoolQuery>> boolQuery = b -> b
            .should(MatchPhrasePrefixQuery.of(m -> m
                    .field("name")
                    .query(input))._toQuery())
            .should(PrefixQuery.of(p -> p
                    .field("brand")
                    .value(input))._toQuery());

    NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(BoolQuery.of(boolQuery)._toQuery())
            .withMaxResults(10)
            .withSourceFilter(new FetchSourceFilter(
                    true, new String[]{"name", "brand", "category"}, null))  // Only fetch specific fields
            .build();

    SearchHits<Product> hits = elasticsearchOperations.search(searchQuery, Product.class);
}
```

### 🚀 **Advanced Features Available:**

- **Query Boosting**: Relevance tuning
- **Source Filtering**: Reduce network overhead
- **Custom Sorting**: Multiple sort criteria
- **Aggregations**: Analytics and faceted search
- **Highlighting**: Search result highlighting
- **Script Queries**: Custom scoring functions

---

## 🏗️ **Service Layer Architecture Patterns**

### **BasicProductService** - Repository-Focused

```java

@Service
public class BasicProductService {
    private final ProductRepository productRepository;

    // Direct repository delegation
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    // Business logic with repository methods
    public List<Product> findFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }
}
```

### **AdvancedSearchService** - ElasticsearchOperations

```java

@Service
public class AdvancedSearchService {
    private final ProductRepository productRepository;           // For simple operations
    private final ElasticsearchOperations elasticsearchOperations; // For complex queries

    // Repository for simple data access
    public List<Product> findActiveProducts() {
        return productRepository.findByActiveTrue();
    }

    // ElasticsearchOperations for advanced features
    public SearchResponse<Product> complexSearch(...) {
        // Use CriteriaQuery or NativeQuery
        return elasticsearchOperations.search(query, Product.class);
    }
}
```

---

## 🎪 **Real-World Usage Examples**

### **Scenario 1: E-commerce Product Filtering**

```java
// ❌ Don't use NativeQuery for this
public List<Product> getProductsByCategory(String category) {
    NativeQuery query = NativeQuery.builder()
            .withQuery(TermQuery.of(t -> t.field("category.keyword").value(category))._toQuery())
            .build();
    return elasticsearchOperations.search(query, Product.class);
}

// ✅ Use Repository Method instead
public List<Product> getProductsByCategory(String category) {
    return productRepository.findByCategory(category);
}
```

### **Scenario 2: Dynamic Search with Multiple Filters**

```java
// ❌ Don't build JSON strings dynamically
public List<Product> dynamicSearch(String query, String category, Double minRating) {
    StringBuilder json = new StringBuilder("{\"bool\":{\"must\":[");
    // ... complex string building
}

// ✅ Use CriteriaQuery instead
public List<Product> dynamicSearch(String query, String category, Double minRating) {
    Criteria criteria = new Criteria();

    if (query != null) {
        criteria = criteria.and(new Criteria("name").matches(query));
    }
    if (category != null) {
        criteria = criteria.and(new Criteria("category.keyword").is(category));
    }
    if (minRating != null) {
        criteria = criteria.and(new Criteria("rating").greaterThanEqual(minRating));
    }

    CriteriaQuery criteriaQuery = new CriteriaQuery(criteria);
    return elasticsearchOperations.search(criteriaQuery, Product.class);
}
```

### **Scenario 3: Advanced Search with Aggregations**

```java
// ✅ NativeQuery is perfect for advanced features
public SearchResponse<Product> searchWithAggregations(String query) {
    NativeQuery searchQuery = NativeQuery.builder()
            .withQuery(MatchQuery.of(m -> m.field("name").query(query))._toQuery())
            .withAggregations(Map.of(
                    "categories", Aggregation.of(a -> a.terms(t -> t.field("category.keyword"))),
                    "avgPrice", Aggregation.of(a -> a.avg(av -> av.field("price")))
            ))
            .build();

    return elasticsearchOperations.search(searchQuery, Product.class);
}
```

---

## 🚦 **Performance Guidelines**

### **Repository Methods** ⚡

- **Fastest** for simple operations
- Optimized by Spring Data
- Minimal overhead

### **@Query Annotations** ⚡⚡

- Pre-compiled queries
- Good performance for complex logic
- Cached by Spring

### **CriteriaQuery** ⚡⚡

- Slight overhead from query building
- Good for dynamic scenarios
- Type-safe validation

### **NativeQuery** ⚡⚡⚡

- Maximum control over performance
- Direct Elasticsearch API access
- Best for complex operations

---

## 📋 **Best Practices Summary**

### 🟢 **DO:**

- Start with Repository methods for simple cases
- Use @Query for custom business logic
- Choose CriteriaQuery for dynamic, type-safe queries
- Reserve NativeQuery for advanced Elasticsearch features
- Combine approaches in a single service when needed

### 🔴 **DON'T:**

- Use NativeQuery for simple CRUD operations
- Build JSON strings manually
- Mix query approaches unnecessarily
- Ignore type safety when available
- Over-engineer simple queries

---

## 🎓 **Learning Path Recommendation**

1. **Start Here**: Master Repository query methods
2. **Level Up**: Learn @Query annotations for custom logic
3. **Advanced**: Use CriteriaQuery for dynamic queries
4. **Expert**: Leverage NativeQuery for advanced features

Each approach builds on the previous one, giving you a complete toolkit for any Elasticsearch scenario! 🚀

---

## 🔗 **Related Files in Codebase**

- `ProductRepository.java` - Repository methods and @Query examples
- `BasicProductService.java` - Repository-focused service pattern
- `AdvancedSearchService.java` - ElasticsearchOperations patterns
- `Product.java` - Entity mapping and field configurations