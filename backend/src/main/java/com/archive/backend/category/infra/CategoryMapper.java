package com.archive.backend.category.infra;

import com.archive.backend.category.domain.Category;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryParent;
import com.archive.backend.category.domain.CategoryTitle;
import java.util.UUID;
import org.mapstruct.Mapper;

/**
 * Category ↔ its JPA entity. The parent is restored without the check: it was checked when stored. The position is
 * the repository's to keep.
 */
@Mapper(componentModel = "spring")
public interface CategoryMapper {

    default Category toDomain(CategoryEntity entity) {
        CategoryId id = new CategoryId(entity.getId());
        UUID parent = entity.getParentId();
        return new Category(
                id,
                new CategoryTitle(entity.getTitle()),
                parent == null ? null : CategoryParent.restore(id, new CategoryId(parent)));
    }

    /**
     * The entity of the category at the given place among its siblings.
     */
    default CategoryEntity toEntity(Category category, int position) {
        CategoryParent parent = category.getParent();
        CategoryEntity entity = new CategoryEntity();
        entity.setId(category.getId().value());
        entity.setTitle(category.getTitle().value());
        entity.setParentId(parent == null ? null : parent.id().value());
        entity.setPosition(position);
        return entity;
    }
}
