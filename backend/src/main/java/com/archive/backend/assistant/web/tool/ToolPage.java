package com.archive.backend.assistant.web.tool;

import java.util.List;
import org.springframework.data.domain.Page;

/** A page of a list as a tool answers it: the count of all, the page (first is 1), its size and its items. */
public record ToolPage<T>(long total, int page, int size, List<T> items) {

    public static <T> ToolPage<T> of(Page<T> page) {
        return new ToolPage<>(page.getTotalElements(), page.getNumber() + 1, page.getSize(), page.getContent());
    }
}
