package ru.max.demo.elastic.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;
import ru.max.demo.elastic.model.cateogries.attrs.AttrGroup;

import java.util.List;
import java.util.Map;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "categories")
public class CategoryEntity {

    @Id
    private String id;

    @Field(type = FieldType.Keyword)
    private String name;

    @Field(type = FieldType.Text)
    private String path;

    @Field(type = FieldType.Text, analyzer = "russian")
    private String ruName;

    @Field(type = FieldType.Keyword)
    private String parentId;

    @Field(type = FieldType.Nested)
    private List<Attr> attrs;

    @Field(type = FieldType.Nested)
    private Map<String, AttrGroup> predefinedAttrs;
 }
