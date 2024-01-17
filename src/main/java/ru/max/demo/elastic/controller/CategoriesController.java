package ru.max.demo.elastic.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategies;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import io.swagger.v3.oas.annotations.Operation;
import lombok.RequiredArgsConstructor;
import lombok.SneakyThrows;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ResourceLoader;
import org.springframework.http.HttpEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;
import ru.max.demo.elastic.Response;
import ru.max.demo.elastic.model.CategoriesResponse;
import ru.max.demo.elastic.model.CategoryResponse;
import ru.max.demo.elastic.model.CreateAttrsRequest;
import ru.max.demo.elastic.model.CreateCategoryRequest;
import ru.max.demo.elastic.model.UpdateCategoryRequest;
import ru.max.demo.elastic.model.cateogries.Categories;
import ru.max.demo.elastic.model.cateogries.Category;
import ru.max.demo.elastic.model.cateogries.attrs.Attr;
import ru.max.demo.elastic.model.cateogries.attrs.AttrGroup;
import ru.max.demo.elastic.model.cateogries.attrs.AttrType;
import ru.max.demo.elastic.model.cateogries.attrs.RefAttr;
import ru.max.demo.elastic.service.CategoriesService;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toMap;
import static org.springframework.http.HttpStatus.NOT_FOUND;
import static org.springframework.http.HttpStatus.OK;

@Slf4j
@CrossOrigin
@RestController
@RequestMapping("/categories/v1")
@RequiredArgsConstructor
public class CategoriesController {

    private final ResourceLoader resourceLoader;

    private final ObjectMapper ymlMapper = new ObjectMapper(new YAMLFactory())
            .setPropertyNamingStrategy(PropertyNamingStrategies.KEBAB_CASE);
    private final String path = "classpath:categories/categories.yml";

    private final CategoriesService categoriesService;

    @SneakyThrows
    @PostMapping("/init")
    public void initCategories() {
        var categories = ymlMapper.readValue(resourceLoader.getResource(path).getFile(), Categories.class);
        var predefinedAttrs = categories.getAttributes();
        var commonAttrs = categories.getCommonAttrs();

        enrichAttrs(categories.getAttributes(), commonAttrs);
        enrichCategories(categories.getAttributes(), commonAttrs, categories.getCategories());
        categoriesService.createRoot(commonAttrs, predefinedAttrs);
        categoriesService.saveCategoriesTree(categories.getCategories());
    }

    @GetMapping
    @SneakyThrows
    @Operation(description = "Getting top level categories")
    public CategoriesResponse getCategories() {
        var cats = categoriesService.findAll();
        return CategoriesResponse.builder()
                .categories(cats)
                .build();
    }

    @GetMapping("/{id}/sub-categories")
    @SneakyThrows
    @Operation(description = "Getting top level categories")
    public CategoriesResponse getSubCategories(@PathVariable("id") String categoryId) {
        var cats = categoriesService.findSubCategoriesOf(categoryId);
        return CategoriesResponse.builder()
                .categories(cats)
                .build();
    }

    @GetMapping("/{id}")
    @Operation(description = "Get category by id")
    public CategoryResponse getCategory(@PathVariable("id") String id) {
        return categoriesService.findCategory(id)
                .map(this::toCategoryResponse)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @PostMapping
    @Operation(description = "Create category")
    public CategoryResponse createCategory(@RequestBody CreateCategoryRequest request) {
        var category = Category.builder()
                .name(request.getName())
                .ruName(request.getRuName())
                .parentId(request.getParentId())
                .build();
        return CategoryResponse.builder()
                .category(categoriesService.saveCategory(category))
                .build();
    }

    @DeleteMapping("/{id}")
    @Operation(description = "Delete category")
    public Response deleteCategory(@PathVariable("id") String categoryId) {
        categoriesService.deleteCategory(categoryId);
        return Response.builder()
                .status(OK)
                .build();
    }

    @PutMapping("/{id}")
    @Operation(description = "Update category")
    public CategoryResponse updateCategory(@PathVariable("id") String categoryId,
                                           @RequestBody UpdateCategoryRequest request) {
        return CategoryResponse.builder()
                .category(categoriesService.updateCategory(categoryId, request))
                .build();
    }

    @GetMapping("/attrs/types")
    @Operation(description = "Get all attribute type")
    public List<String> attrsTypes() {
        return Arrays.stream(AttrType.values())
                .map(AttrType::getValue)
                .toList();
    }

    @GetMapping("/attrs/predefined")
    @Operation(description = "Get predefined attributes")
    public Map<String, AttrGroup> attrsPredefined() {
        return categoriesService.getRoot().getPredefinedAttrs();
    }

    @GetMapping("/{id}/attrs")
    @Operation(description = "Get attributes of the category")
    public List<Attr> getAttrs(@PathVariable("id") String id) {
        return categoriesService.findCategory(id)
                .map(Category::getAttrs)
                .orElseThrow(() -> new ResponseStatusException(NOT_FOUND));
    }

    @DeleteMapping("/{categoryId}/attrs/{attrId}")
    @Operation(description = "Delete attribute from the category")
    public Response deleteAttr(@PathVariable("categoryId") String categoryId,
                        @PathVariable("attrId") String attrId) {
        categoriesService.deleteAttr(categoryId, attrId);
        return Response.builder()
                .status(OK)
                .build();
    }

    @PostMapping("/{categoryId}/attrs")
    @Operation(description = "Delete attribute from the category")
    public Response addAttr(@PathVariable("categoryId") String categoryId,
                        @RequestBody CreateAttrsRequest request) {
        categoriesService.createAttrs(categoryId, request.getAttrs());
        return Response.builder()
                .status(OK)
                .build();
    }

    private CategoryResponse toCategoryResponse(Category category) {
        return CategoryResponse.builder()
                .category(category)
                .build();
    }

    private void enrichAttrs(Map<String, AttrGroup> attributes, List<Attr> attrsToEnrich) {
        if (attrsToEnrich == null || attrsToEnrich.isEmpty()) {
            return;
        }
        var referredAttrs = new ArrayList<Attr>();
        for (Attr attr : attrsToEnrich) {
            if (attr instanceof RefAttr ref) {
                // ref атрибут ссылается на какие-то существующие в attributes атрибуты
                // поэтому нужно достать атрибуты, на которые он ссылается, и добавить в referredAttrs
                Optional.ofNullable(attributes.get(ref.getKey()))
                        .ifPresentOrElse(
                                attrs -> {
                                    referredAttrs.addAll(extractRefAttr(attrs.getAttrs(), ref));
                                },
                                () -> {
                                    throw new IllegalStateException("Attr " + ref.getKey() + " not found among attributes");
                                }
                        );
            }
        }
        attrsToEnrich.removeIf(attr -> attr instanceof RefAttr);
        attrsToEnrich.addAll(referredAttrs);
    }

    private List<Attr> extractRefAttr(List<Attr> referredAttrs, RefAttr ref) {
        if (referredAttrs == null || referredAttrs.isEmpty()) {
            log.warn("Ref attr " + ref.getName() + " references empty attribute");
            return List.of();
        }
        if (ref.getNames() == null) {
            return referredAttrs;
        } else {
            var attrByName = referredAttrs.stream()
                    .collect(toMap(Attr::getName, Function.identity()));
            return ref.getNames().stream()
                    .filter(attrByName::containsKey)
                    .map(attrByName::get)
                    .collect(Collectors.toList());
        }
    }

    private void enrichCategories(Map<String, AttrGroup> attributes, List<Attr> commonAttrs, List<Category> categories) {
        if (categories != null) {
            categories.forEach(category -> {
                category.getAttrs().addAll(commonAttrs);
                enrichAttrs(attributes, category.getAttrs());
                enrichCategories(attributes, commonAttrs, category.getSubCategories());
            });
        }
    }
}
