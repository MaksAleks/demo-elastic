package ru.max.demo.elastic.model;

import lombok.Builder;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;

import java.util.List;

@Value
@Builder
@Jacksonized
public class CreateAttrsRequest {

    List<Attr> attrs;
}
