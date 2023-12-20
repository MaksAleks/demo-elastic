package ru.max.demo.elastic.model.cateogries.attrs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
public class Option {
    String name;
    String ruName;
}
