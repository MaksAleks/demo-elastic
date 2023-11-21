package ru.max.demo.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    private String category;
    private String name;
    private String description;
    private Double price;
    private Double rating;
    private Double popularity;
    private Location location;
}
