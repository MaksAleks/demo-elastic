package ru.max.demo.elastic.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpMethod;
import org.springframework.web.client.RestTemplate;

@RequiredArgsConstructor
public class OpenSearchClient {

    private final ObjectMapper om;

    private final OpenSearchClientProperties props;

    private final RestTemplate client;

    public String createIndex(String indexName) {
        var uri = props.getUri() + "/" + indexName;
        return client.exchange(uri, HttpMethod.PUT, null, String.class).getBody();
    }
}
