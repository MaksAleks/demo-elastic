package ru.max.demo.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequest {
    private String searchString;
    private List<Filter> filters;
    private String sortBy = SortBy.SCORE.getValue();
    private String nextPage;
}
