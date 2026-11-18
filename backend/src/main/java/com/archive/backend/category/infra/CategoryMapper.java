package com.archive.backend.category.infra;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.category.domain.CategoryTitles;
import com.archive.backend.shared.domain.Language;
import java.util.UUID;
import java.util.stream.Collectors;
import org.mapstruct.Mapper;

/**
 * Category ↔ its JPA entity, its titles kept by the codes of their languages. The parent is restored without the check: it was checked when stored. The position is
 * the repository's to keep.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    default Category toDomain(CategoryEntity entity) {
        CategoryId id = new CategoryId(entity.getId());
        UUID parent = entity.getParentId();
        return new Category(
                id,
                new CategoryTitles(entity.getTitles().entrySet().stream()
                        .collect(Collectors.toMap(
                                title -> Language.known(title.getKey()),
                                title -> new CategoryTitle(title.getValue())))),
                parent == null ? null : CategoryParent.restore(id, new CategoryId(parent)));
    }

    /**
     * The entity of the category at the given place among its siblings.
     */
    default CategoryEntity toEntity(Category category, int position) {
        CategoryParent parent = category.getParent();
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId().value());
        entity.setTitles(category.getTitles().values().entrySet().stream()
                .collect(Collectors.toMap(
                        title -> title.getKey().code(),
                        title -> title.getValue().value())));
        entity.setParentId(parent == null ? null : parent.id().value());
        entity.setPosition(position);
        return entity;
    }
}
