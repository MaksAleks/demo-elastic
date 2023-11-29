package ru.max.demo.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import org.opensearch.index.query.BoolQueryBuilder;
import org.opensearch.index.query.QueryBuilders;

import java.util.Objects;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RangeFilter extends Filter {

    private Range<?> value;

    @Override
    public void applyTo(BoolQueryBuilder boolQueryBuilder) {
        boolQueryBuilder.filter(QueryBuilders.rangeQuery(field)
                .from(Objects.requireNonNull(value.getFrom()))
                .to(Objects.requireNonNull(value.getTo())));
    }
}
