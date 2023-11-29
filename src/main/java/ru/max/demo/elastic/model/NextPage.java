package ru.max.demo.elastic.model;

import lombok.Builder;
import lombok.RequiredArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Value
@Builder
@Jacksonized
@RequiredArgsConstructor
public class NextPage {

    String pit;
    List<Object> sortValues;
}
