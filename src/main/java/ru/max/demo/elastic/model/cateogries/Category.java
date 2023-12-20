package ru.max.demo.elastic.model.cateogries;

import com.fasterxml.jackson.annotation.JsonInclude;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;

import java.util.ArrayList;
import java.util.List;

@Value
@Builder
@Jacksonized
public class Category {

    @Schema(description = "идентификатор категории", requiredMode = Schema.RequiredMode.REQUIRED)
    String id;
    @Schema(description = "путь к директории", requiredMode = Schema.RequiredMode.REQUIRED)
    String path;
    @Schema(description = "идентификатор родительское категории категории", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String parentId;
    @Schema(description = "название категории", requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    String name;
    @Schema(description = "русское название категории", requiredMode = Schema.RequiredMode.REQUIRED)
    String ruName;
    @Schema(description = "атрибута категории", requiredMode = Schema.RequiredMode.REQUIRED)
    @Builder.Default
    List<Attr> attrs = new ArrayList<>();
    @Builder.Default
    @Schema(description = "список подкатегориой", requiredMode = Schema.RequiredMode.REQUIRED)
    List<Category> subCategories = new ArrayList<>();
}
