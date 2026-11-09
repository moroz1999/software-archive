package com.archive.backend.author.web;

import com.archive.backend.author.app.GetAuthorHandler;
import com.archive.backend.author.app.GetAuthorQuery;
import com.archive.backend.author.app.ListAuthorsHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.web.dto.AuthorCatalogEntryDto;
import com.archive.backend.author.web.dto.AuthorDetailsDto;
import com.archive.backend.author.web.mapper.AuthorWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.UUID;

@RestController
@RequestMapping("/api/authors")
@RequiredArgsConstructor
public class AuthorController {

    private final ListAuthorsHandler listHandler;
    private final GetAuthorHandler getHandler;
    private final AuthorWebMapper webMapper;

    @GetMapping
    public Page<AuthorCatalogEntryDto> list(
            @RequestParam(required = false) @Nullable String letter,
            @RequestParam(required = false) @Nullable UUID country,
            @RequestParam(required = false) @Nullable UUID city,
            Pageable pageable
    ) {
        return listHandler.handle(webMapper.toListQuery(letter, country, city, pageable)).map(webMapper::toCatalogEntry);
    }

    @GetMapping("/{id}")
    public AuthorDetailsDto get(@PathVariable UUID id) {
        return webMapper.toDetails(getHandler.handle(new GetAuthorQuery(new AuthorId(id))));
    }
}
