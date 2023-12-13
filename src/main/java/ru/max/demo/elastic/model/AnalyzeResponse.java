package ru.max.demo.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Value
@Builder
@Jacksonized
public class AnalyzeResponse {

    Map<String, List<String>> pos;

    @JsonIgnore
    public Optional<String> getNoun() {
        return Optional.ofNullable(pos.get("NOUN")).map(list -> list.get(0));
    }


    @JsonIgnore
    public Optional<List<String>> getAdjectives() {
        return Optional.ofNullable(pos.get("ADJ"));
    }
}
