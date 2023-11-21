package ru.max.demo.elastic.client;

import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@RequiredArgsConstructor
@ConfigurationProperties(prefix = "opensearch")
public class OpenSearchClientProperties {

    private final String uri;
    private final String username;
    private final String password;
}
