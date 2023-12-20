package ru.max.demo.elastic.model.cateogries;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;
import ru.max.demo.elastic.model.cateogries.attrs.AttrGroup;

import java.util.List;
import java.util.Map;

@Value
@Builder
@Jacksonized
public class Categories {
    Map<String, AttrGroup> attributes;
    List<Attr> commonAttrs;
    List<Category> categories;
}
