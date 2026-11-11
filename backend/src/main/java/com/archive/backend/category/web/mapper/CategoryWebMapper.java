package com.archive.backend.category.web.mapper;

import com.archive.backend.category.app.CreateCategoryCommand;
import com.archive.backend.category.app.UpdateCategoryCommand;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.web.dto.CategoryCreatedDto;
import com.archive.backend.category.web.dto.CategoryDetailsDto;
import com.archive.backend.category.web.dto.CategoryNodeDto;
import com.archive.backend.category.web.dto.CategoryRequestDto;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;

@Mapper(componentModel = "spring")
public interface CategoryWebMapper {

    CategoryNodeDto toNode(CategoryNode node);

    CategoryDetailsDto toDetails(CategoryDetails details);

    default CategoryCreatedDto toCreated(CategoryId id) {
        return new CategoryCreatedDto(id.value());
    }

    default CreateCategoryCommand toCreate(CategoryRequestDto request) {
        return new CreateCategoryCommand(toTitle(request.title()), toParent(request.parentId()));
    }

    default UpdateCategoryCommand toUpdate(UUID id, CategoryRequestDto request) {
        return new UpdateCategoryCommand(new CategoryId(id), toTitle(request.title()), toParent(request.parentId()));
    }

    private static CategoryTitle toTitle(@Nullable String value) {
        if (value == null) {
            throw new InvalidValueException("title.required", "A category needs a title.");
        }
        return new CategoryTitle(value);
    }

    private static @Nullable CategoryId toParent(@Nullable UUID value) {
        return value == null ? null : new CategoryId(value);
    }
}
