package ru.max.demo.elastic.model.cateogries.attrs.size;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.experimental.SuperBuilder;
import lombok.extern.jackson.Jacksonized;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;

import java.util.List;

@NoArgsConstructor
@Data
@EqualsAndHashCode(callSuper = true)
public class SizeAttr<T> extends Attr {

    String gender;
    String kind;
    List<T> sizes;
}
