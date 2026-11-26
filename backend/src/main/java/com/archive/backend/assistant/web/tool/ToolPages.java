package com.archive.backend.assistant.web.tool;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;

/**
 * The page a tool reads: the first one when none is given, of as many entries as the page of that list on the site, or
 * 50 for a list the site has no page of; never more than a hundred.
 */
public final class ToolPages {

    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_SIZE = 100;

    /** The schema of the page fields, to put into a tool's properties. */
    public static final String SCHEMA = """
            "page": {"type": ["integer", "null"], "description": "The page, the first is 1."},
            "size": {"type": ["integer", "null"], "description": "Entries per page, as many as the page on the site when not given, at most 100."}
            """;

    private ToolPages() {}

    public static PageRequest of(@Nullable Integer page, @Nullable Integer size) {
        return of(page, size, DEFAULT_SIZE);
    }

    /** The page of a list the site has a page of, as many entries as that page when none are asked for. */
    public static PageRequest of(@Nullable Integer page, @Nullable Integer size, int siteSize) {
        int number = page == null ? 0 : Math.max(page - 1, 0);
        int entries = size == null ? siteSize : Math.clamp(size, 1, MAX_SIZE);
        return PageRequest.of(number, entries);
    }
}
