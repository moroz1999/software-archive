package com.archive.backend.author.web;

import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.app.DeleteAuthorCommand;
import com.archive.backend.author.app.DeleteAuthorHandler;
import com.archive.backend.author.app.GetAuthorHandler;
import com.archive.backend.author.app.GetAuthorQuery;
import com.archive.backend.author.app.ListAuthorsHandler;
import com.archive.backend.author.app.UpdateAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.web.dto.AuthorCatalogEntryDto;
import com.archive.backend.author.web.dto.AuthorCreatedDto;
import com.archive.backend.author.web.dto.AuthorDetailsDto;
import com.archive.backend.author.web.dto.AuthorRequestDto;
import com.archive.backend.author.web.mapper.AuthorWebMapper;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final ListAuthorsHandler listHandler;
    private final GetAuthorHandler getHandler;
    private final CreateAuthorHandler createHandler;
    private final UpdateAuthorHandler updateHandler;
    private final DeleteAuthorHandler deleteHandler;
    private final AuthorWebMapper webMapper;

    @GetMapping
    public Page<AuthorCatalogEntryDto> list(
            @RequestParam(required = false) @Nullable String letter,
            @RequestParam(required = false) @Nullable UUID country,
            @RequestParam(required = false) @Nullable UUID city,
            @RequestParam(required = false) @Nullable String platform,
            Pageable pageable) {
        return listHandler
                .handle(webMapper.toListQuery(letter, country, city, platform, pageable))
                .map(webMapper::toCatalogEntry);
    }

    @GetMapping("/{id}")
    public AuthorDetailsDto get(@PathVariable UUID id) {
        return webMapper.toDetails(getHandler.handle(new GetAuthorQuery(new AuthorId(id))));
    }

    @PostMapping
    public ResponseEntity<AuthorCreatedDto> create(@RequestBody AuthorRequestDto request) {
        AuthorId id = createHandler.handle(webMapper.toCreate(request));
        return ResponseEntity.created(URI.create("/api/authors/" + id.value())).body(webMapper.toCreated(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @RequestBody AuthorRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(id, request));
    }

    /**
     * Deletes the person; their labels stay in the archive as labels of nobody.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteHandler.handle(new DeleteAuthorCommand(new AuthorId(id)));
    }
}
