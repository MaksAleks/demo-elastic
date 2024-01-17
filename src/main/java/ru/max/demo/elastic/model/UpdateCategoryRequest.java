package ru.max.demo.elastic.model;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class UpdateCategoryRequest {

    public String name;
    public String ruName;
}
