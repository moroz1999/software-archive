package com.archive.backend.category.web.mapper;

import com.archive.backend.category.app.CreateCategoryCommand;
import com.archive.backend.category.app.UpdateCategoryCommand;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.domain.CategoryTitle;
import com.archive.backend.category.domain.CategoryTitles;
import com.archive.backend.category.read.CategoryDetails;
import com.archive.backend.category.read.CategoryNode;
import com.archive.backend.category.web.dto.CategoryCreatedDto;
import com.archive.backend.category.web.dto.CategoryDetailsDto;
import com.archive.backend.category.web.dto.CategoryNodeDto;
import com.archive.backend.category.web.dto.CategoryRequestDto;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.Actor;
import java.util.HashMap;
import java.util.Map;
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

    default CreateCategoryCommand toCreate(Actor actor, CategoryRequestDto request) {
        return new CreateCategoryCommand(actor, toTitles(request.titles()), toParent(request.parentId()));
    }

    default UpdateCategoryCommand toUpdate(Actor actor, UUID id, CategoryRequestDto request) {
        return new UpdateCategoryCommand(
                actor, new CategoryId(id), toTitles(request.titles()), toParent(request.parentId()));
    }

    private static CategoryTitles toTitles(@Nullable Map<String, String> values) {
        if (values == null) {
            throw new InvalidValueException("title.required", "A category needs a title.");
        }
        Map<Language, CategoryTitle> titles = new HashMap<>();
        values.forEach((code, title) -> titles.put(Language.known(code), new CategoryTitle(title)));
        return new CategoryTitles(titles);
    }

    private static @Nullable CategoryId toParent(@Nullable UUID value) {
        return value == null ? null : new CategoryId(value);
    }
}
