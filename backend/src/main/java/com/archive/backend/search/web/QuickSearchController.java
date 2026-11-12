package com.archive.backend.search.web;

import com.archive.backend.search.app.QuickSearchHandler;
import com.archive.backend.search.web.dto.QuickSearchDto;
import com.archive.backend.search.web.mapper.QuickSearchWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The quick search (docs/features/search.md#rest). */
@RestController
@RequiredArgsConstructor
public class QuickSearchController {

    private final QuickSearchHandler handler;
    private final QuickSearchWebMapper webMapper;

    @GetMapping("/api/search/quick")
    public QuickSearchDto search(
            @RequestParam(name = "q", required = false) @Nullable String keyword,
            @RequestParam(required = false) @Nullable String platform) {
        return webMapper.toResult(handler.handle(webMapper.toQuery(keyword, platform)));
    }
}
