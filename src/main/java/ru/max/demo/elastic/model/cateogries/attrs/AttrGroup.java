package ru.max.demo.elastic.model.cateogries.attrs;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.Value;
import lombok.extern.jackson.Jacksonized;

import java.util.List;

@Data
@NoArgsConstructor
public class AttrGroup {

    String ruName;
    List<Attr> attrs;
}
