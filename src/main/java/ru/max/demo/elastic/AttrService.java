package ru.max.demo.elastic;

import org.springframework.stereotype.Service;
import ru.max.demo.elastic.model.Attribute;

import java.util.List;
import java.util.Map;

@Service
public class AttrService {

    private Map<String, List<Attribute>> attrsByCategory = Map.of(
            "декор для дома", List.of(
                    Attribute.builder().name("color").boost(4).build(),
                    Attribute.builder().name("material").boost(1).build()
            )
    );

    public List<Attribute> getAttrs(String category) {
        return attrsByCategory.getOrDefault(category, List.of());
    }
}
