package ru.max.demo.elastic;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import ru.max.demo.elastic.model.AnalyzeRequest;
import ru.max.demo.elastic.model.AnalyzeResponse;

import java.util.Objects;

@Service
public class NlpService {

    private final RestTemplate restTemplate = new RestTemplate();

    public AnalyzeResponse analyzeText(String text) {
        var rq = AnalyzeRequest.builder()
                .text(text)
                .build();
        System.out.println("Trying to analyze text: " + text);
        var response = restTemplate.postForEntity("http://localhost:8765/analyze", rq, AnalyzeResponse.class).getBody();
        Objects.requireNonNull(response);
        return response;
    }
}
