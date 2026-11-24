package com.archive.backend.assistant.web.entry;

import java.util.UUID;

/** The id of the entry a tool reads. */
public record IdArguments(UUID id) {

    static final String SCHEMA = """
            {
              "type": "object",
              "properties": {"id": {"type": "string", "format": "uuid"}},
              "required": ["id"],
              "additionalProperties": false
            }
            """;
}
