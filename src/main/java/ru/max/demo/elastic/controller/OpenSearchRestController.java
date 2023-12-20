package ru.max.demo.elastic.controller;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import org.apache.lucene.search.join.ScoreMode;
import org.opensearch.OpenSearchStatusException;
import org.opensearch.action.search.CreatePitRequest;
import org.opensearch.client.ResponseException;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.common.unit.Fuzziness;
import org.opensearch.common.unit.TimeValue;
import org.opensearch.data.client.orhlc.NativeSearchQueryBuilder;
import org.opensearch.data.client.orhlc.OpenSearchRestTemplate;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.MultiMatchQueryBuilder;
import org.opensearch.index.query.NestedQueryBuilder;
import org.opensearch.index.query.QueryBuilders;
import org.opensearch.search.aggregations.AggregationBuilders;
import org.opensearch.search.sort.SortBuilder;
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
import ru.max.demo.elastic.AttrService;
import ru.max.demo.elastic.NlpService;
import ru.max.demo.elastic.mapper.ProductMapper;
import ru.max.demo.elastic.model.AnalyzeResponse;
import ru.max.demo.elastic.model.FilterRequest;
import ru.max.demo.elastic.model.FilterResponse;
import ru.max.demo.elastic.model.NextPage;
import ru.max.demo.elastic.model.Product;
import ru.max.demo.elastic.model.ProductIdx;
import ru.max.demo.elastic.model.SearchRequest;
import ru.max.demo.elastic.model.SearchResponse;
import ru.max.demo.elastic.model.SortBy;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Base64;
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
    private final ProductMapper mapper;
    private final NlpService nlpService;
    private final AttrService attrService;

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
//
//    @PostMapping("/{name}/search")
//    public SearchResponse search(@PathVariable("name") String indexName, @RequestBody SearchRequest request) {
//        var analyzeResponse = nlpService.analyzeText(request.getSearchString());
//        var nounOpt = analyzeResponse.getNoun();
//
//        var category = nounOpt.flatMap(noun -> findCategory(indexName, noun));
//
//        var nextPage = getNextPage(request.getNextPage());
//        String pit = nextPage.map(NextPage::getPit)
//                .orElseGet(() -> openPit(indexName));
//
//        // Constructing the bool query
//        var boolQueryBuilder = QueryBuilders.boolQuery();
//
//        var nameStdQuery = QueryBuilders.matchQuery("name.std", request.getSearchString())
//                .boost(2)
//                .fuzziness(Fuzziness.AUTO)
//                .prefixLength(1)
//                .maxExpansions(10);
//
//        var nameQuery = QueryBuilders.matchQuery("name", request.getSearchString())
//                .boost(1)
//                .fuzziness(Fuzziness.AUTO)
//                .prefixLength(1)
//                .maxExpansions(10);
//
//        var descriptionQuery = QueryBuilders.matchQuery("description", request.getSearchString());
//
//        nounOpt.ifPresent(noun -> {
//            var nameNounQuery = QueryBuilders.matchQuery("name_noun", noun);
//            boolQueryBuilder.must(nameNounQuery);
//        });
//        boolQueryBuilder.should(nameQuery);
//        boolQueryBuilder.should(nameStdQuery);
//        boolQueryBuilder.should(descriptionQuery);
//
//        category.ifPresent(cat -> {
//            analyzeResponse.getAdjectives()
//                    .ifPresent(adjList -> {
//                        var attrValues = String.join(" ", adjList);
//                        attrService.getAttrs(cat).forEach(attr -> {
//                            var path = "filter";
//                            var match = QueryBuilders.matchQuery(path + "." + attr.getName(), attrValues)
//                                    .boost(attr.getBoost());
//
//                            var queryBuilder = QueryBuilders.nestedQuery(path, match, ScoreMode.Max)
//                                    .boost(attr.getBoost());
//                            boolQueryBuilder.should(
//                                    queryBuilder
//                            );
//                        });
//                    });
//        });
//
//        // Applying filters
//        Optional.ofNullable(request.getFilters())
//                .ifPresent(filters -> {
//                    filters.forEach(filter -> filter.applyTo(boolQueryBuilder));
//                });
//
//
//        var sortBuilders = new ArrayList<SortBuilder<?>>();
//        Optional.ofNullable(request.getSortBy())
//                .ifPresentOrElse(
//                        sortBy -> {
//                            sortBuilders.add(SortBuilders.fieldSort(sortBy).order(SortOrder.DESC));
//                            sortBuilders.add(SortBuilders.scoreSort());
//                        },
//                        () -> {
//                            sortBuilders.add(SortBuilders.scoreSort());
//                            sortBuilders.add(SortBuilders.fieldSort(SortBy.SCORE.getValue()).order(SortOrder.DESC));
//                        });
//
//        // Building the native search query
//        var queryBuilder = new NativeSearchQueryBuilder()
//                .withQuery(boolQueryBuilder)
//                .withSorts(sortBuilders) // Modify as needed
//                .withPageable(PageRequest.of(0, 100)) // Modify as needed
//                .withTrackTotalHits(false)
//                .withPointInTime(new Query.PointInTime(pit, Duration.ofMinutes(10))); // Adjust time as needed
//
//        nextPage.ifPresent(np -> {
//            queryBuilder.withSearchAfter(np.getSortValues());
//        });
//
//        var query = queryBuilder.build();
//        System.out.println(query.getQuery());
//
//        // Execute the search
//        var searchHits = client.search(query, Product.class, IndexCoordinates.of(indexName));
//        var products = searchHits.getSearchHits().stream()
//                .map(SearchHit::getContent)
//                .collect(Collectors.toList());
//
//        return SearchResponse.builder()
//                .pit(pit)
//                .nextPage(getNextPageBase64(pit, extractLastSortValues(searchHits, products.size() - 1)))
//                .products(products)
//                .build();
//    }

    @SneakyThrows
    private Optional<NextPage> getNextPage(String nextPage64) {
        return Optional.ofNullable(nextPage64)
                .map(str -> Base64.getDecoder().decode(str))
                .map(this::deserNextPage);
    }

    @SneakyThrows
    private NextPage deserNextPage(byte[] decoded) {
        return objectMapper.readValue(decoded, NextPage.class);
    }

    @SneakyThrows
    private String getNextPageBase64(String pit, List<Object> sort) {
        if (sort.isEmpty()) {
            return null;
        }
        var nextPage = NextPage.builder()
                .pit(pit)
                .sortValues(sort)
                .build();
        var str = objectMapper.writeValueAsString(nextPage);
        return Base64.getEncoder().encodeToString(str.getBytes(StandardCharsets.UTF_8));
    }

    private List<Object> extractLastSortValues(SearchHits<?> searchHits, int last) {
        if (last > -1) {
            return searchHits.getSearchHit(last).getSortValues();
        } else {
            return List.of();
        }
    }

    @PostMapping("/create-test-data")
    @ResponseStatus(HttpStatus.CREATED)
    public void createTestData() throws Exception {
        Resource resource = resourceLoader.getResource("classpath:test-data.json");
        List<Product> products = objectMapper.readValue(
                Files.readAllBytes(Paths.get(resource.getURI())),
                new TypeReference<>() {
                });

        List<ProductIdx> analyzedProducts = products.stream()
                .map(mapper::toProductIdx)
                .toList();

        List<IndexQuery> queries = analyzedProducts.stream()
                .peek(product -> product.setScore(1))
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
