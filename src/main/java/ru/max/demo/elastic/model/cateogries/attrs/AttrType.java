package ru.max.demo.elastic.model.cateogries.attrs;

import com.fasterxml.jackson.annotation.JsonValue;

public enum AttrType {

    TEXT("text"),
    COLORS("colors"),
    UNITS("units"),
    DATE("date"),
    LIST("list"),
    OPTIONS("options"),
    MAP("map"),
    COMPOUND("compound"),
    SIZES("sizes"),
    DIMENSIONS("dimensions"),
    REFERENCE("ref");

    private final String value;

    @JsonValue
    public String getValue() {
        return value;
    }
//
//    @JsonCreator
//    public static AttrType fromName(String name) {
//        return Arrays.stream(AttrType.values())
//                .filter(op -> op.getName().equals(name))
//                .findFirst()
//                .orElseThrow(() -> new IllegalArgumentException("Unknown unit: " + name));
//    }

    AttrType(String value) {
        this.value = value;
    }
}
