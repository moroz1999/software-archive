package com.archive.backend.category.web;

import com.archive.backend.category.app.CreateCategoryHandler;
import com.archive.backend.category.app.DeleteCategoryCommand;
import com.archive.backend.category.app.DeleteCategoryHandler;
import com.archive.backend.category.app.GetCategoryHandler;
import com.archive.backend.category.app.GetCategoryQuery;
import com.archive.backend.category.app.ListCategoriesHandler;
import com.archive.backend.category.app.ListCategoriesQuery;
import com.archive.backend.category.app.UpdateCategoryHandler;
import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.category.web.dto.CategoryCreatedDto;
import com.archive.backend.category.web.dto.CategoryDetailsDto;
import com.archive.backend.category.web.dto.CategoryNodeDto;
import com.archive.backend.category.web.dto.CategoryRequestDto;
import com.archive.backend.category.web.mapper.CategoryWebMapper;
import java.net.URI;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/categories")
@RequiredArgsConstructor
public class CategoryController {

    private final ListCategoriesHandler listHandler;
    private final GetCategoryHandler getHandler;
    private final CreateCategoryHandler createHandler;
    private final UpdateCategoryHandler updateHandler;
    private final DeleteCategoryHandler deleteHandler;
    private final CategoryWebMapper webMapper;

    /**
     * The whole tree at once: it is small, and every view of it needs all of it.
     */
    @GetMapping
    public List<CategoryNodeDto> list() {
        return listHandler.handle(new ListCategoriesQuery()).stream()
                .map(webMapper::toNode)
                .toList();
    }

    @GetMapping("/{id}")
    public CategoryDetailsDto get(@PathVariable UUID id) {
        return webMapper.toDetails(getHandler.handle(new GetCategoryQuery(new CategoryId(id))));
    }

    @PostMapping
    public ResponseEntity<CategoryCreatedDto> create(@RequestBody CategoryRequestDto request) {
        CategoryId id = createHandler.handle(webMapper.toCreate(request));
        return ResponseEntity.created(URI.create("/api/categories/" + id.value()))
                .body(webMapper.toCreated(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @RequestBody CategoryRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(id, request));
    }

    /**
     * Deletes the category with its subcategories; their software only leaves them.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteHandler.handle(new DeleteCategoryCommand(new CategoryId(id)));
    }
}
