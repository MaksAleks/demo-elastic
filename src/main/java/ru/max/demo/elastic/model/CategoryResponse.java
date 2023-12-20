package ru.max.demo.elastic.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.max.demo.elastic.model.cateogries.Category;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;

import java.util.List;

@Value
@Builder
@Jacksonized
@JsonInclude(JsonInclude.Include.NON_NULL)
public class CategoryResponse {

    @Schema(description = "каегория продуктов", requiredMode = Schema.RequiredMode.REQUIRED)
    Category category;
}
