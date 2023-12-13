package ru.max.demo.elastic.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Value
@Builder
@Jacksonized
public class AnalyzeRequest {

    String text;
}
