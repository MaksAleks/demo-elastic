package ru.max.demo.elastic.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonSubTypes;
import com.fasterxml.jackson.annotation.JsonTypeInfo;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.opensearch.index.query.BoolQueryBuilder;

@JsonTypeInfo(
        use = JsonTypeInfo.Id.NAME,
        include = JsonTypeInfo.As.EXTERNAL_PROPERTY,
        property = "type"
)
@JsonSubTypes({
        @JsonSubTypes.Type(value = SingleValueFilter.class, name = "single"),
        @JsonSubTypes.Type(value = RangeFilter.class, name = "range"),
})
@Data
@NoArgsConstructor
@AllArgsConstructor
public abstract class Filter {

    public static final String PATH = "filter.%s";

    private String type;

    protected String field;

    @JsonIgnore
    protected String getFieldPath() {
        return String.format(PATH, field);
    }

    public abstract void applyTo(BoolQueryBuilder query);
}
