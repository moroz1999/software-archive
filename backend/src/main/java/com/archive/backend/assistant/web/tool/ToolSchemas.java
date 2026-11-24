package com.archive.backend.assistant.web.tool;

import java.util.Collection;
import java.util.stream.Collectors;

/** Pieces of the JSON Schemas of tools. */
public final class ToolSchemas {

    private ToolSchemas() {}

    /** The values as a JSON array of strings, sorted, for an {@code enum}. */
    public static String strings(Collection<String> values) {
        return values.stream().sorted().map(value -> "\"" + value + "\"").collect(Collectors.joining(", ", "[", "]"));
    }
}
