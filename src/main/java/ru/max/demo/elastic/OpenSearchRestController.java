package ru.max.demo.elastic;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.search.CreatePitRequest;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.data.client.orhlc.NativeSearchQuery;
import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder;
import org.opensearch.data.client.orhlc.OpenSearchRestTemplate;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.sort.SortBuilders;
import org.opensearch.search.sort.SortOrder;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.elasticsearch.core.SearchHit;
import org.springframework.data.elasticsearch.core.SearchHits;
import org.springframework.data.elasticsearch.core.mapping.IndexCoordinates;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.data.elasticsearch.core.query.IndexQueryBuilder;
import org.springframework.data.elasticsearch.core.query.Query;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import ru.max.demo.elastic.model.FilterRequest;
import ru.max.demo.elastic.model.FilterResponse;
import ru.max.demo.elastic.model.Product;
import ru.max.demo.elastic.model.SearchRequest;

import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import static org.opensearch.client.RequestOptions.DEFAULT;

@CrossOrigin
@RestController
@RequestMapping("/index")
@RequiredArgsConstructor
public class OpenSearchRestController {

    private final RestHighLevelClient osClient;
    private final OpenSearchRestTemplate client;
    private final ResourceLoader resourceLoader;
    private final ObjectMapper objectMapper;

    @PostMapping("/{name}")
    @ResponseStatus(HttpStatus.CREATED)
    public void createIndex(@PathVariable("name") String name) {
        client.indexOps(IndexCoordinates.of(name)).create();
    }

    @PostMapping("/{name}/product")
    @ResponseStatus(HttpStatus.CREATED)
    public void createProduct(@PathVariable("name") String name, @RequestBody Product product) {
        IndexQuery indexQuery = new IndexQueryBuilder()
                .withId(name) // Optionally set a custom ID for the document
                .withObject(product)
                .build();
        client.index(indexQuery, IndexCoordinates.of(name));
    }

    @PostMapping("/search/{index}")
    public List<Product> search(@PathVariable("index") String indexName, @RequestBody SearchRequest request) {
        var index = IndexCoordinates.of(indexName);
        var boolQueryBuilder = QueryBuilders.boolQuery();

        boolQueryBuilder.should(QueryBuilders.matchQuery("name", request.getSearchString()));
        boolQueryBuilder.should(QueryBuilders.matchQuery("description", request.getSearchString()));

        NativeSearchQuery searchQuery = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .build();

        return client.search(searchQuery, Product.class, index).getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());
    }

    @PostMapping("/filter/{index}")
    public FilterResponse filter(@PathVariable("index") String indexName, @RequestBody FilterRequest request) {
        String pit = Optional.ofNullable(request.getPit())
                .orElseGet(() -> openPit(indexName));

        // Constructing the bool query
        BoolQueryBuilder boolQueryBuilder = QueryBuilders.boolQuery();

        // Adding category filter if provided
        if (request.getCategory() != null && !request.getCategory().isEmpty()) {
            boolQueryBuilder.filter(QueryBuilders.termQuery("category", request.getCategory()));
        }

        // Adding price range filter if provided
        if (request.getPrice() != null && request.getPrice().getFrom() != null && request.getPrice().getTo() != null) {
            boolQueryBuilder.filter(QueryBuilders.rangeQuery("price")
                    .from(request.getPrice().getFrom())
                    .to(request.getPrice().getTo()));
        }

        // Building the native search query
        NativeSearchQueryBuilder queryBuilder = new NativeSearchQueryBuilder()
                .withQuery(boolQueryBuilder)
                .withSorts(SortBuilders.fieldSort(request.getSort().getSortBy()).order(SortOrder.DESC)) // Modify as needed
                .withPageable(PageRequest.of(0, 4)) // Modify as needed
                .withTrackTotalHits(false)
                .withPointInTime(new Query.PointInTime(pit, Duration.ofMinutes(10))); // Adjust time as needed

        if (request.getSort().getValue() != null) {
            queryBuilder.withSearchAfter(List.of(request.getSort().getValue()));
        }

        // Execute the search
        SearchHits<Product> searchHits = client.search(queryBuilder.build(), Product.class, IndexCoordinates.of(indexName));
        List<Product> products = searchHits.getSearchHits().stream()
                .map(SearchHit::getContent)
                .collect(Collectors.toList());

        return FilterResponse.builder()
                .pit(pit)
                .products(products)
                .build();
    }

    @PostMapping("/create-test-data")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTestData() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:test-data.json");
        List<Product> products = objectMapper.readValue(
                Files.readAllBytes(Paths.get(resource.getURI())),
                new TypeReference<>() {
                });

        List<IndexQuery> queries = products.stream()
                .map(product -> new IndexQueryBuilder()
                        .withObject(product)
                        .build())
                .collect(Collectors.toList());

        client.bulkIndex(queries, IndexCoordinates.of("products"));
    }

    @GetMapping("/image")
    @SneakyThrows
    public void getImage(@RequestParam("path") String imagePath, HttpServletResponse res) {
        new ClassPathResource("images/cat.png").getInputStream().transferTo(res.getOutputStream());
    }

    @SneakyThrows
    @ExceptionHandler(OpenSearchStatusException.class)
    public void handle(OpenSearchStatusException ex, HttpServletResponse response) {
        response.addHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE);
        for (var supEx : ex.getSuppressed()) {
            if (supEx instanceof ResponseException respEx) {
                respEx.getResponse().getEntity().writeTo(response.getOutputStream());
            }
        }
    }

    @SneakyThrows
    private String openPit(String indexName) {
        var rq = new CreatePitRequest(TimeValue.timeValueMinutes(10), false, indexName);
        return osClient.createPit(rq, DEFAULT).getId();
    }
}
