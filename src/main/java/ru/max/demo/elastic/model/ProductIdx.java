package ru.max.demo.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class ProductIdx {

    private String category;
    private String name;
    private String name_noun;
    private List<String> name_adj;
    private String description;
    private Double price;
    private Double rating;
    private Double popularity;
    private Location location;
    private Integer score;
    private Map<String, String> filter;
}
