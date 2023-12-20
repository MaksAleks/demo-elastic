package ru.max.demo.elastic.model.cateogries.attrs;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonValue;
import lombok.RequiredArgsConstructor;

import java.util.Arrays;

@RequiredArgsConstructor
public enum Unit {

    MILLI_METER("mm"), CENTI_METER("cm"), METER("m"),

    MILLI_GRAM("mg"), GRAM("g"), KILO_GRAM("kg"),

    MILLI_LITRE("ml"), LITRE("l");


    @JsonIgnore
    private final String unit;

    @JsonValue
    public String getUnit() {
        return unit;
    }

    @JsonCreator
    public static Unit forValue(String value) {
        return Arrays.stream(Unit.values())
                .filter(op -> op.getUnit().equals(value))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Unknown unit: " + value));
    }
}
