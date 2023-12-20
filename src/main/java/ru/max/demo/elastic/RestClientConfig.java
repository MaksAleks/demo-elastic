package ru.max.demo.elastic;

import lombok.SneakyThrows;
import org.opensearch.client.RestHighLevelClient;
import org.opensearch.data.client.orhlc.AbstractOpenSearchConfiguration;
import org.opensearch.data.client.orhlc.ClientConfiguration;
import org.opensearch.data.client.orhlc.OpenSearchRestTemplate;
import org.opensearch.data.client.orhlc.RestClients;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.elasticsearch.core.ElasticsearchOperations;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.X509TrustManager;
import java.security.SecureRandom;
import java.security.cert.X509Certificate;

@Configuration
public class RestClientConfig extends AbstractOpenSearchConfiguration {

    @Override
    @Bean
    @SneakyThrows
    public RestHighLevelClient opensearchClient() {
        final ClientConfiguration clientConfiguration = ClientConfiguration.builder()
                .connectedTo("localhost:9200")
                .withBasicAuth("admin", "admin")
                .build();
        return RestClients.create(clientConfiguration).rest();
    }
//
//    @Bean
//    public OpenSearchRestTemplate openSearchRestTemplate(RestHighLevelClient client) {
//        return new OpenSearchRestTemplate(client);
//    }
}