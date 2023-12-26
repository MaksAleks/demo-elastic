package ru.max.demo.elastic.model.cateogries.attrs;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonSetter;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import ru.max.demo.elastic.model.cateogries.attrs.size.SizeAttr;

import java.time.ZonedDateTime;
import java.util.UUID;

@NoArgsConstructor
@EqualsAndHashCode
@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXISTING_PROPERTY,
        property = "type",
        visible = true
)
@JsonSubTypes({
        @JsonSubTypes.Type(
                value = PlainAttr.class,
                names = {"text", "date", "list"}
        ),
        @JsonSubTypes.Type(
                value = OptionsAttr.class,
                name = "options"
        ),
        @JsonSubTypes.Type(
                value = ColorAttr.class,
                name = "colors"
        ),
        @JsonSubTypes.Type(
                value = UnitsAttr.class,
                name = "units"
        ),
        @JsonSubTypes.Type(
                value = RefAttr.class,
                name = "ref"
        ),
        @JsonSubTypes.Type(
                value = CompoundAttr.class,
                name = "compound"
        ),
        @JsonSubTypes.Type(
                value = MapAttr.class,
                name = "map"
        ),
        @JsonSubTypes.Type(
                value = SizeAttr.class,
                name = "sizes"
        ),

        @JsonSubTypes.Type(
                value = DimensionsAttr.class,
                name = "dimensions"
        ),

})
@Data
public abstract class Attr {

    @Schema(description = "идентификатор атрибута", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "attrId", required = true)
    private String attrId = UUID.randomUUID().toString();
    @Schema(description = "тип атрибута", requiredMode = Schema.RequiredMode.REQUIRED)
    private AttrType type;
    private String name;
    @Schema(description = "название атрибута", requiredMode = Schema.RequiredMode.REQUIRED)
    private String ruName;
    @Schema(description = "показатель обязатености", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "required", required = true)
    private Boolean required = true;
    @Schema(description = "показатель использования атрибута для фильтрации", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonProperty(value = "faceted", required = true)
    private Boolean faceted = true;
    @Schema(description = "дата создания", requiredMode = Schema.RequiredMode.REQUIRED)
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd'T'HH:mm:ss.SSSZ")
    @Field(type = FieldType.Date, format = DateFormat.date_time, pattern = "uuuu-MM-dd'T'HH:mm:ss.SSSZ")
    ZonedDateTime dateCreated;
}
