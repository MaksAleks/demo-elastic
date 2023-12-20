package ru.max.demo.elastic.model.cateogries.attrs;

import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class CompoundAttr extends Attr {

    List<Attr> attrs;
}
