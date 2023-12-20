package ru.max.demo.elastic.model.cateogries.attrs.size;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class BootsSize extends Size {

    String rus;
    Float len;
}

