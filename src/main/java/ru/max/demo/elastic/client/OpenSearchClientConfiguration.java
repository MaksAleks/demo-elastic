package ru.max.demo.elastic.client;

import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@RequiredArgsConstructor
public class OpenSearchClientConfiguration {

    @Bean
    public OpenSearchClient openSearchClient(OpenSearchClientProperties props, ObjectMapper om) {
        var client = new RestTemplateBuilder()
                .basicAuthentication(props.getUsername(), props.getPassword())
                .build();
        client.setRequestFactory(new SkipSslVerificationHttpRequestFactory());
        return new OpenSearchClient(om, props, client);
    }

}
