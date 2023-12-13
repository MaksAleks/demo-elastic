package ru.max.demo.elastic.model;

import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class Attribute {

    String name;
    int boost;
}
