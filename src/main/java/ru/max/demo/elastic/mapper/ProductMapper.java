package ru.max.demo.elastic.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import ru.max.demo.elastic.model.Product;
import ru.max.demo.elastic.model.ProductIdx;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public interface ProductMapper {

    ProductIdx toProductIdx(Product product);

    Product toProduct(ProductIdx productIdx);
}
