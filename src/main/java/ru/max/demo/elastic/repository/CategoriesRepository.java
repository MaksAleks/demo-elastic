package ru.max.demo.elastic.repository;

import org.springframework.data.repository.CrudRepository;
import ru.max.demo.elastic.entity.CategoryEntity;
import ru.max.demo.elastic.entity.CategoryProjection;

import java.util.List;

public interface CategoriesRepository extends CrudRepository<CategoryEntity, String> {

    List<CategoryEntity> findAllByParentId(String parentId);
}
