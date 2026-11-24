package com.archive.backend.assistant.web.entry;

import java.util.UUID;

/** A release, by its software and its own id. */
public record GetReleaseArguments(UUID softwareId, UUID releaseId) {

    public static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "softwareId": {"type": "string", "format": "uuid"},
                "releaseId": {"type": "string", "format": "uuid"}
              },
              "required": ["softwareId", "releaseId"],
              "additionalProperties": false
            }
            """;
}
