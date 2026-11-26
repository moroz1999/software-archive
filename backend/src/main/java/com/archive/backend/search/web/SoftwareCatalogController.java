package com.archive.backend.search.web;

import com.archive.backend.search.app.ListSoftwareCatalogHandler;
import com.archive.backend.search.app.ListSoftwareCatalogQuery;
import com.archive.backend.search.web.dto.SoftwareCatalogDto;
import com.archive.backend.search.web.dto.SoftwareCatalogParams;
import com.archive.backend.search.web.mapper.SoftwareCatalogWebMapper;
import com.archive.backend.shared.domain.Language;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The software catalog (docs/features/software-list.md#rest). */
@RestController
@RequiredArgsConstructor
public class SoftwareCatalogController {

    private final ListSoftwareCatalogHandler handler;
    private final SoftwareCatalogWebMapper webMapper;

    @GetMapping("/api/software")
    public SoftwareCatalogDto list(
            @RequestParam(name = "q", required = false) @Nullable String keyword,
            @RequestParam(required = false) @Nullable String platform,
            @RequestParam(required = false) @Nullable Integer year,
            @RequestParam(required = false) @Nullable UUID category,
            @RequestParam(required = false) @Nullable UUID country,
            @RequestParam(required = false) @Nullable UUID city,
            @RequestParam(required = false) @Nullable List<String> platforms,
            @RequestParam(required = false) @Nullable List<String> hardware,
            Language language,
            @PageableDefault(size = ListSoftwareCatalogQuery.PAGE_SIZE) Pageable pageable) {
        return webMapper.toCatalog(handler.handle(webMapper.toQuery(
                new SoftwareCatalogParams(keyword, platform, year, category, country, city, platforms, hardware),
                language,
                pageable)));
    }
}
