package com.archive.backend.search.web;

import com.archive.backend.search.app.FullSearchHandler;
import com.archive.backend.search.web.dto.FoundAuthorDto;
import com.archive.backend.search.web.dto.FoundGroupDto;
import com.archive.backend.search.web.dto.FoundPageDto;
import com.archive.backend.search.web.mapper.FullSearchWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The full search of people and of groups (docs/features/search.md#rest). */
@RestController
@RequiredArgsConstructor
public class FullSearchController {

    private final FullSearchHandler handler;
    private final FullSearchWebMapper webMapper;

    @GetMapping("/api/search/authors")
    public FoundPageDto<FoundAuthorDto> authors(
            @RequestParam(name = "q", required = false) @Nullable String keyword,
            @RequestParam(required = false) @Nullable String platform,
            Pageable pageable) {
        return webMapper.toAuthors(handler.authors(webMapper.toQuery(keyword, platform, pageable)));
    }

    @GetMapping("/api/search/groups")
    public FoundPageDto<FoundGroupDto> groups(
            @RequestParam(name = "q", required = false) @Nullable String keyword,
            @RequestParam(required = false) @Nullable String platform,
            Pageable pageable) {
        return webMapper.toGroups(handler.groups(webMapper.toQuery(keyword, platform, pageable)));
    }
}
