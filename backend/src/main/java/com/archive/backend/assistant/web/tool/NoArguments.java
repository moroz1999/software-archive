package com.archive.backend.assistant.web.tool;

/** The arguments of a tool that takes none. */
public record NoArguments() {

    public static final String SCHEMA = """
            {"type": "object", "properties": {}, "additionalProperties": false}
            """;
}
