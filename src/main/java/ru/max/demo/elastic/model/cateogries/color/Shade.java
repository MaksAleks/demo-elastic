package ru.max.demo.elastic.model.cateogries.color;

import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

@Data
@NoArgsConstructor
public class Shade {
    String name;
    String hex;
}
