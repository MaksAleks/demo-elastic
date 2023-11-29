package ru.max.demo.elastic.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
public class SearchResponse {

    String pit;
    List<Product> products;
    String nextPage;
}
