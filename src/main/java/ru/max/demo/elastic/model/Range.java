package ru.max.demo.elastic.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class Range<T> {

    private T from;
    private T to;

    public static <T> Range<T> of(T from, T to) {
        return new Range<T>(from, to);
    }
}
