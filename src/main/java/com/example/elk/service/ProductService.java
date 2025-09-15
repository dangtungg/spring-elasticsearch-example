package com.example.elk.service;

import co.elastic.clients.elasticsearch._types.SortOptions;
import co.elastic.clients.elasticsearch._types.SortOrder;
import co.elastic.clients.elasticsearch._types.aggregations.*;
import co.elastic.clients.elasticsearch._types.query_dsl.BoolQuery;
import co.elastic.clients.elasticsearch._types.query_dsl.Query;
import com.example.elk.model.Product;
import com.example.elk.model.SearchResponse;
import com.example.elk.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.elasticsearch.client.elc.NativeQuery;
import org.springframework.data.elasticsearch.client.elc.NativeQueryBuilder;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.query.SourceFilter;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProductService {

    private final ProductRepository productRepository;

    private final ElasticsearchOperations elasticsearchOperations;

    private static final String PRODUCT_INDEX = "products";

    // Basic CRUD Operations
    public Product save(Product product) {
        product.setUpdatedAt(LocalDateTime.now());
        return productRepository.save(product);
    }

    public Optional<Product> findById(String id) {
        return productRepository.findById(id);
    }

    public void deleteById(String id) {
        productRepository.deleteById(id);
    }

    public List<Product> findAll() {
        return (List<Product>) productRepository.findAll();
    }

    // Bulk Operations
    public List<Product> saveAll(List<Product> products) {
        products.forEach(product -> product.setUpdatedAt(LocalDateTime.now()));
        return (List<Product>) productRepository.saveAll(products);
    }

    public void deleteAll() {
        productRepository.deleteAll();
    }

    // Search Operations
    public SearchResponse<Product> searchProducts(String query, int page, int size) {
        long startTime = System.currentTimeMillis();

        Pageable pageable = PageRequest.of(page, size);

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.matchQuery("name", query).boost(2.0f))
                .should(QueryBuilders.matchQuery("description", query))
                .should(QueryBuilders.matchQuery("category", query))
                .should(QueryBuilders.matchQuery("brand", query))
                .minimumShouldMatch(1);

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(pageable)
                .withSort(SortBuilders.scoreSort().order(SortOrder.DESC))
                .build();

        SearchHits<Product> searchHits = elasticsearchOperations.search(searchQuery, Product.class);

        List<Product> products = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        SearchResponse<Product> response = new SearchResponse<>(products, searchHits.getTotalHits(), page, size);
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        return response;
    }

    // Advanced Search with Filters
    public SearchResponse<Product> advancedSearch(String query, String category, String brand,
                                                  BigDecimal minPrice, BigDecimal maxPrice,
                                                  Double minRating, Boolean inStockOnly,
                                                  int page, int size, String sortBy, String sortDir) {
        long startTime = System.currentTimeMillis();

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery();

        // Text search
        if (query != null && !query.trim().isEmpty()) {
            BoolQueryBuilder textQuery = QueryBuilders.boolQuery()
                    .should(QueryBuilders.matchQuery("name", query).boost(2.0f))
                    .should(QueryBuilders.matchQuery("description", query))
                    .minimumShouldMatch(1);
            boolQuery.must(textQuery);
        }

        // Filters
        if (category != null && !category.isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("category.keyword", category));
        }

        if (brand != null && !brand.isEmpty()) {
            boolQuery.filter(QueryBuilders.termQuery("brand.keyword", brand));
        }

        if (minPrice != null || maxPrice != null) {
            var rangeQuery = QueryBuilders.rangeQuery("price");
            if (minPrice != null) rangeQuery.gte(minPrice);
            if (maxPrice != null) rangeQuery.lte(maxPrice);
            boolQuery.filter(rangeQuery);
        }

        if (minRating != null) {
            boolQuery.filter(QueryBuilders.rangeQuery("rating").gte(minRating));
        }

        if (inStockOnly != null && inStockOnly) {
            boolQuery.filter(QueryBuilders.rangeQuery("stockQuantity").gt(0));
        }

        boolQuery.filter(QueryBuilders.termQuery("active", true));

        // Sorting
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withPageable(PageRequest.of(page, size));

        if (sortBy != null && !sortBy.isEmpty()) {
            SortOrder order = "desc".equalsIgnoreCase(sortDir) ? SortOrder.DESC : SortOrder.ASC;
            switch (sortBy.toLowerCase()) {
                case "price":
                    queryBuilder.withSort(SortBuilders.fieldSort("price").order(order));
                    break;
                case "rating":
                    queryBuilder.withSort(SortBuilders.fieldSort("rating").order(order));
                    break;
                case "name":
                    queryBuilder.withSort(SortBuilders.fieldSort("name.keyword").order(order));
                    break;
                case "created":
                    queryBuilder.withSort(SortBuilders.fieldSort("createdAt").order(order));
                    break;
                default:
                    queryBuilder.withSort(SortBuilders.scoreSort().order(SortOrder.DESC));
            }
        }

        SearchHits<Product> searchHits = elasticsearchOperations.search(queryBuilder.build(), Product.class);

        List<Product> products = searchHits.stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        SearchResponse<Product> response = new SearchResponse<>(products, searchHits.getTotalHits(), page, size);
        response.setSearchTimeMs(System.currentTimeMillis() - startTime);

        return response;
    }

    // Aggregations
    public Map<String, Object> getProductAggregations() {
        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(QueryBuilders.termQuery("active", true))
                .addAggregation(AggregationBuilders.terms("categories").field("category.keyword").size(10))
                .addAggregation(AggregationBuilders.terms("brands").field("brand.keyword").size(10))
                .addAggregation(AggregationBuilders.histogram("priceRanges").field("price").interval(50))
                .addAggregation(AggregationBuilders.avg("avgRating").field("rating"))
                .addAggregation(AggregationBuilders.sum("totalProducts").field("stockQuantity"))
                .withMaxResults(0) // We only want aggregations, not documents
                .build();

        SearchHits<Product> searchHits = elasticsearchOperations.search(searchQuery, Product.class);

        Map<String, Object> aggregations = new HashMap<>();

        if (searchHits.getAggregations() != null) {
            // Process aggregations
            searchHits.getAggregations().asMap().forEach((name, aggregation) -> {
                if (aggregation instanceof Terms) {
                    Terms termsAgg = (Terms) aggregation;
                    List<Map<String, Object>> buckets = termsAgg.getBuckets().stream()
                            .map(bucket -> Map.of(
                                    "key", bucket.getKeyAsString(),
                                    "count", bucket.getDocCount()
                            ))
                            .collect(Collectors.toList());
                    aggregations.put(name, buckets);
                } else {
                    aggregations.put(name, aggregation);
                }
            });
        }

        return aggregations;
    }

    // Suggestions/Autocomplete
    public List<String> getSuggestions(String input) {
        if (input == null || input.length() < 2) {
            return Collections.emptyList();
        }

        BoolQueryBuilder boolQuery = QueryBuilders.boolQuery()
                .should(QueryBuilders.prefixQuery("name", input.toLowerCase()))
                .should(QueryBuilders.prefixQuery("brand", input.toLowerCase()))
                .should(QueryBuilders.prefixQuery("category", input.toLowerCase()));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQuery)
                .withMaxResults(10)
                .withSourceFilter(new org.springframework.data.elasticsearch.core.query.FetchSourceFilter(
                        new String[]{"name", "brand", "category"}, null))
                .build();

        SearchHits<Product> searchHits = elasticsearchOperations.search(searchQuery, Product.class);

        Set<String> suggestions = new HashSet<>();
        searchHits.forEach(hit -> {
            Product product = hit.getContent();
            if (product.getName().toLowerCase().startsWith(input.toLowerCase())) {
                suggestions.add(product.getName());
            }
            if (product.getBrand() != null && product.getBrand().toLowerCase().startsWith(input.toLowerCase())) {
                suggestions.add(product.getBrand());
            }
            if (product.getCategory() != null && product.getCategory().toLowerCase().startsWith(input.toLowerCase())) {
                suggestions.add(product.getCategory());
            }
        });

        return new ArrayList<>(suggestions);
    }

    // Repository-based methods
    public List<Product> findByCategory(String category) {
        return productRepository.findByCategory(category);
    }

    public Page<Product> findByCategory(String category, int page, int size) {
        return productRepository.findByCategory(category, PageRequest.of(page, size));
    }

    public List<Product> findByPriceRange(BigDecimal minPrice, BigDecimal maxPrice) {
        return productRepository.findByPriceBetween(minPrice, maxPrice);
    }

    public List<Product> findFeaturedProducts() {
        return productRepository.findByFeaturedTrue();
    }

    public List<Product> findHighRatedProducts(Double minRating) {
        return productRepository.findByRatingGreaterThanEqual(minRating);
    }
}

