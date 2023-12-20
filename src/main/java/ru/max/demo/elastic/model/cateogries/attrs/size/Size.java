package ru.max.demo.elastic.model.cateogries.attrs.size;

import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "kind"
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = ClothesSize.class,
                name = "clothes"
        ),
        @JsonSubTypes.Type(
                value = BootsSize.class,
                name = "boots"
        ),
})
public abstract class Size {

}
