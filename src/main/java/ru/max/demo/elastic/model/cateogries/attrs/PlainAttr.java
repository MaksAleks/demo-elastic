package ru.max.demo.elastic.model.cateogries.attrs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;

@EqualsAndHashCode(callSuper = true)
@Data
@NoArgsConstructor
public class PlainAttr extends Attr {
}
