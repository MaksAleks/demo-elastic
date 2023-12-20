package ru.max.demo.elastic.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
public class CreateCategoryRequest {

    public String name;
    public String ruName;
    @JsonProperty(value = "parentId", required = true)
    public String parentId = "root";
}
