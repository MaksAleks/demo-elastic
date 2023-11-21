package ru.max.demo.elastic;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.data.elasticsearch.ElasticsearchDataAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import ru.max.demo.elastic.client.OpenSearchClientProperties;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Stream;

@SpringBootApplication(exclude = {ElasticsearchDataAutoConfiguration.class})
@EnableConfigurationProperties(OpenSearchClientProperties.class)
public class DemoElasticApplication {
    public static void main(String[] args) {
        SpringApplication.run(DemoElasticApplication.class, args);
    }

}
