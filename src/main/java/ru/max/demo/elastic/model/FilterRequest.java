package ru.max.demo.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class FilterRequest {

    private String pit;
    private String category;
    private Range<Double> price;
    private String sortBy = SortBy.SCORE.getValue();
    private List<Object> nextPage;
}
