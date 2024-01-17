package ru.max.demo.elastic.service;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import ru.max.demo.elastic.entity.CategoryEntity;
import ru.max.demo.elastic.model.CategoryResponse;
import ru.max.demo.elastic.model.CreateCategoryRequest;
import ru.max.demo.elastic.model.UpdateCategoryRequest;
import ru.max.demo.elastic.model.cateogries.Category;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;
import ru.max.demo.elastic.model.cateogries.attrs.AttrGroup;
import ru.max.demo.elastic.repository.CategoriesRepository;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Comparator;
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
            var bulk = createEntities(category);
            categoriesRepository.saveAll(bulk);
        });
    }

    public Category saveCategory(Category category) {
        var entity = categoriesRepository.save(createEntity(category));
        return toCategory(entity);
    }

    public Category updateCategory(String categoryId, UpdateCategoryRequest request) {
        return findCategoryEntity(categoryId)
                .map(entity -> entity.setName(request.getName()))
                .map(entity -> entity.setRuName(request.getRuName()))
                .map(categoriesRepository::save)
                .map(this::toCategoryWithoutAttrs)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    public void deleteCategory(String categoryId) {
        categoriesRepository.deleteById(categoryId);
        categoriesRepository.findAllByParentId(categoryId).forEach(cat -> {
            deleteCategory(cat.getId());
        });
    }

    public void createAttrs(String categoryId, List<Attr> attrs) {
        var category = getCategoryEntity(categoryId);

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

    private CategoryEntity getCategoryEntity(String categoryId) {
        return categoriesRepository.findById(categoryId)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    private Optional<CategoryEntity> findCategoryEntity(String categoryId) {
        return categoriesRepository.findById(categoryId);
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
        return result.stream()
                .sorted(Comparator.comparing(Category::getDateCreated))
                .toList();
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
            category.getSubCategories().sort(Comparator.comparing(Category::getDateCreated));
        }
    }

    private Category toCategory(CategoryEntity entity) {
        entity.getAttrs().sort(Comparator.comparing(Attr::getDateCreated));
        return Category.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .ruName(entity.getRuName())
                .path(entity.getPath())
                .attrs(entity.getAttrs())
                .dateCreated(entity.getDateCreated())
                .build();
    }

    private Category toCategoryWithoutAttrs(CategoryEntity entity) {
        return Category.builder()
                .id(entity.getId())
                .parentId(entity.getParentId())
                .name(entity.getName())
                .ruName(entity.getRuName())
                .path(entity.getPath())
                .dateCreated(entity.getDateCreated())
                .build();
    }

    private List<CategoryEntity> createEntities(Category category) {
        var result = new ArrayList<CategoryEntity>();
        createEntities(category, getRoot(), result);
        return result;
    }

    private void createEntities(Category category, CategoryEntity parentEntity, List<CategoryEntity> entities) {
        var id = UUID.randomUUID().toString();
        var path = parentEntity.getId().equals("root") ? id : parentEntity.getPath() + "." + id;
        category.getAttrs().forEach(attr -> attr.setDateCreated(ZonedDateTime.now()));
        var current = CategoryEntity.builder()
                .id(id)
                .parentId(parentEntity.getId())
                .name(category.getName())
                .ruName(category.getRuName())
                .attrs(category.getAttrs())
                .dateCreated(ZonedDateTime.now())
                .path(path)
                .build();
        entities.add(current);
        if (category.getSubCategories() != null) {
            for (var subCategory : category.getSubCategories()) {
                createEntities(subCategory, current, entities);
            }
        }
    }

    private CategoryEntity createEntity(Category category) {
        var parent = getCategoryEntity(category.getParentId());
        var id = UUID.randomUUID().toString();
        var path = parent.getId().equals("root") ? id : parent.getPath() + "." + id;
        category.getAttrs().forEach(attr -> attr.setDateCreated(ZonedDateTime.now()));
        return CategoryEntity.builder()
                .id(id)
                .parentId(parent.getId())
                .name(category.getName())
                .ruName(category.getRuName())
                .dateCreated(ZonedDateTime.now())
                .attrs(category.getAttrs())
                .path(path)
                .build();
    }
}
