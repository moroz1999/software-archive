package com.archive.backend.search.web;

import com.archive.backend.search.app.FullSearchHandler;
import com.archive.backend.search.web.dto.FullSearchDto;
import com.archive.backend.search.web.mapper.FullSearchWebMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** The full search (docs/features/search.md#rest). */
@RestController
@RequiredArgsConstructor
public class FullSearchController {

    private final FullSearchHandler handler;
    private final FullSearchWebMapper webMapper;

    @GetMapping("/api/search")
    public FullSearchDto search(
            @RequestParam(name = "q", required = false) @Nullable String keyword,
            @RequestParam(required = false) @Nullable String platform,
            @RequestParam(required = false) @Nullable List<String> types,
            Pageable pageable) {
        return webMapper.toResult(handler.handle(webMapper.toQuery(keyword, platform, types, pageable)));
    }
}
