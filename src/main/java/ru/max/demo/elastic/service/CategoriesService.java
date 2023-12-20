package ru.max.demo.elastic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.max.demo.elastic.entity.CategoryEntity;
import ru.max.demo.elastic.model.cateogries.Category;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;
import ru.max.demo.elastic.model.cateogries.attrs.AttrGroup;
import ru.max.demo.elastic.repository.CategoriesRepository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.StreamSupport;

import static java.util.stream.Collectors.groupingBy;
import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.CONFLICT;
import static org.springframework.http.HttpStatus.INTERNAL_SERVER_ERROR;
import static org.springframework.http.HttpStatus.NOT_FOUND;

@Service
@RequiredArgsConstructor
public class CategoriesService {

    private final CategoriesRepository categoriesRepository;

    public void createRoot(List<Attr> commonAttrs, Map<String, AttrGroup> predefinedAttrs) {
        var rootCategory = CategoryEntity.builder()
                .id("root")
                .name("root")
                .ruName("корневая категория")
                .attrs(commonAttrs)
                .predefinedAttrs(predefinedAttrs)
                .build();
        categoriesRepository.save(rootCategory);
    }

    public void saveCategoriesTree(List<Category> categories) {
        categories.forEach(category -> {
            var bulk = createEntities(category, "root");
            categoriesRepository.saveAll(bulk);
        });
    }

    public Category saveCategory(Category category) {
        var entity = categoriesRepository.save(createEntity(category));
        return toCategory(entity);
    }

    public void deleteCategory(String categoryId) {
        categoriesRepository.deleteById(categoryId);
        categoriesRepository.findAllByParentId(categoryId).forEach(cat -> {
            deleteCategory(cat.getId());
        });
    }

    public void createAttrs(String categoryId, List<Attr> attrs) {
        var category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));

        var attrsByRuName = category.getAttrs().stream()
                .collect(toMap(Attr::getRuName, Function.identity()));
        var existingAttrs = new ArrayList<String>();
        for (var attr : attrs) {
            if (attrsByRuName.containsKey(attr.getRuName())) {
                existingAttrs.add(attr.getRuName());
            }
        }
        if (!existingAttrs.isEmpty()) {
            throw new ResponseStatusException(CONFLICT, "Following attrs already exists: [" + String.join(", ", existingAttrs) + "]");
        }

        category.getAttrs().addAll(attrs);
        categoriesRepository.save(category);
    }

    public boolean deleteAttr(String categoryId, String attrId) {
        var category = categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
        var removed = category.getAttrs().removeIf(attr -> attr.getAttrId().equals(attrId));
        if (removed) {
            categoriesRepository.save(category);
        }
        return removed;
    }

    public Optional<Category> findCategory(String id) {
        return categoriesRepository.findById(id)
                .map(this::toCategory)
                .map(category -> {
                    enrichSubcategories(category, categoriesRepository::findAllByParentId);
                    return category;
                });
    }

    public CategoryEntity getRoot() {
        return categoriesRepository.findById("root")
                .orElseThrow(() -> new ResponseStatusException(INTERNAL_SERVER_ERROR, "Root category not found"));
    }

    private CategoryEntity getCategory(String categoryId) {
        return categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    public List<Category> findTopLevelCategories() {
        return findSubCategoriesOf("root");
    }

    public List<Category> findSubCategoriesOf(String parentId) {
        return categoriesRepository.findAllByParentId(parentId).stream()
                .map(this::toCategoryWithoutAttrs)
                .toList();
    }

    public List<Category> findAll() {
        var all = StreamSupport.stream(categoriesRepository.findAll().spliterator(), false)
                .toList();
        var roots = all.stream()
                .filter(cat -> Objects.nonNull(cat.getParentId()) && cat.getParentId().equals("root"))
                .toList();

        var categoriesByParentId = all.stream()
                .filter(cat -> Objects.nonNull(cat.getParentId()) && !cat.getParentId().equals("root"))
                .collect(groupingBy(CategoryEntity::getParentId));


        List<Category> result = new ArrayList<>();
        for (var root : roots) {
            var category = toCategoryWithoutAttrs(root);
            enrichSubcategoriesWithoutAttrs(category, categoriesByParentId::get);
            result.add(category);
        }
        return result;
    }

    private void enrichSubcategories(Category category, Function<String, List<CategoryEntity>> getSubCategories) {
        var subEntities = getSubCategories.apply(category.getId());
        if (subEntities != null) {
            var subCategories = subEntities.stream()
                    .map(this::toCategory)
                    .toList();
            subCategories.forEach(subCat -> enrichSubcategories(subCat, getSubCategories));
            category.getSubCategories().addAll(subCategories);
        }
    }

    private void enrichSubcategoriesWithoutAttrs(Category category, Function<String, List<CategoryEntity>> getSubCategories) {
        var subEntities = getSubCategories.apply(category.getId());
        if (subEntities != null) {
            var subCategories = subEntities.stream()
                    .map(this::toCategoryWithoutAttrs)
                    .toList();
            subCategories.forEach(subCat -> enrichSubcategoriesWithoutAttrs(subCat, getSubCategories));
            category.getSubCategories().addAll(subCategories);
        }
    }

    private Category toCategory(CategoryEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .ruName(entity.getRuName())
                .attrs(entity.getAttrs())
                .build();
    }

    private Category toCategoryWithoutAttrs(CategoryEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .ruName(entity.getRuName())
                .build();
    }

    private List<CategoryEntity> createEntities(Category category, String parentId) {
        var result = new ArrayList<CategoryEntity>();
        createEntities(category, parentId, result);
        return result;
    }

    private void createEntities(Category category, String parentId, List<CategoryEntity> entities) {
        var parent = CategoryEntity.builder()
                .id(UUID.randomUUID().toString())
                .parentId(parentId)
                .name(category.getName())
                .ruName(category.getRuName())
                .attrs(category.getAttrs())
                .build();
        entities.add(parent);
        if (category.getSubCategories() != null) {
            for (var subCategory : category.getSubCategories()) {
                createEntities(subCategory, parent.getId(), entities);
            }
        }
    }

    private CategoryEntity createEntity(Category category) {
        var parent = getCategory(category.getParentId());
        return CategoryEntity.builder()
                .id(UUID.randomUUID().toString())
                .parentId(parent.getId())
                .name(category.getName())
                .ruName(category.getRuName())
                .attrs(category.getAttrs())
                .build();
    }
}
