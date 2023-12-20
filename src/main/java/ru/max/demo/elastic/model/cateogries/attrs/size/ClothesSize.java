package ru.max.demo.elastic.model.cateogries.attrs.size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@NoArgsConstructor
public class ClothesSize extends Size {

    String rus;
    String uni;
    Param breast;
    Param waist;
    Param belt;
    Param hips;

    @Data
    @NoArgsConstructor
    public static class Param {
        String ruName;
        String value;
    }
}

