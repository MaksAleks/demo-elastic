package ru.max.demo.elastic;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.view.InternalResourceViewResolver;

@Configuration
public class SpringDocConfig {
    @Value("${swagger.context.root:${server.servlet.context-path}}")
    private String contextRoot;

    @Bean
    public OpenAPI meetingInfoOpenAPI() {
        return new OpenAPI()
                .addServersItem(new Server().url(contextRoot));
    }

    @Bean
    public InternalResourceViewResolver defaultViewResolver() {
        return new InternalResourceViewResolver();
    }
}
