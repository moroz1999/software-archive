package com.archive.backend.assistant.web.entry;

import java.util.UUID;

/** The id of the entry a tool reads or changes. */
public record IdArguments(UUID id) {

    public static final String SCHEMA = """
            {
              "type": "object",
              "properties": {"id": {"type": "string", "format": "uuid"}},
              "required": ["id"],
              "additionalProperties": false
            }
            """;
}
