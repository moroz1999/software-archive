package com.archive.backend.assistant.web.tool;

import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A place where a person, a group or a label is located: a country, or a city of it; one of them is the primary. */
public record LocationArguments(
        @Nullable UUID countryId,
        @Nullable UUID cityId,
        @Nullable Boolean primary) {

    public static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "countryId": {"type": ["string", "null"], "format": "uuid"},
                "cityId": {"type": ["string", "null"], "format": "uuid"},
                "primary": {"type": ["boolean", "null"], "description": "The place they are best known from."}
              },
              "additionalProperties": false
            }
            """;
}
