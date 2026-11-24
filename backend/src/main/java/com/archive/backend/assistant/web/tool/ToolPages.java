package com.archive.backend.assistant.web.tool;

import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.PageRequest;

/** The page a tool reads: the first one when none is given, of 50 entries, never more than a hundred. */
public final class ToolPages {

    public static final int DEFAULT_SIZE = 50;
    public static final int MAX_SIZE = 100;

    /** The schema of the page fields, to put into a tool's properties. */
    public static final String SCHEMA = """
            "page": {"type": ["integer", "null"], "description": "The page, the first is 1."},
            "size": {"type": ["integer", "null"], "description": "Entries per page, 50 when not given, at most 100."}
            """;

    private ToolPages() {}

    public static PageRequest of(@Nullable Integer page, @Nullable Integer size) {
        int number = page == null ? 0 : Math.max(page - 1, 0);
        int entries = size == null ? DEFAULT_SIZE : Math.clamp(size, 1, MAX_SIZE);
        return PageRequest.of(number, entries);
    }
}
