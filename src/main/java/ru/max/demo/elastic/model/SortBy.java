package ru.max.demo.elastic.model;

import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
public enum SortBy {

    SCORE("score"),
    POPULARITY("popularity"),
    PRICE("price");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
}