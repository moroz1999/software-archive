package com.archive.backend.assistant.web.search;

import com.archive.backend.assistant.web.tool.ToolPages;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The arguments of a catalog of names: its letter, its place and its page, as the catalog page has them. */
public record CatalogArguments(
        @Nullable String letter,
        @Nullable UUID countryId,
        @Nullable UUID cityId,
        @Nullable String platform,
        @Nullable Integer page,
        @Nullable Integer size) {

    static final String SCHEMA = """
            {
              "type": "object",
              "properties": {
                "letter": {"type": ["string", "null"], "description": "A–Z, А–Я, or # for any other first character."},
                "countryId": {"type": ["string", "null"], "format": "uuid"},
                "cityId": {"type": ["string", "null"], "format": "uuid"},
                "platform": {"type": ["string", "null"], "description": "The platform of the platform switch."},
                %s
              },
              "additionalProperties": false
            }
            """.formatted(ToolPages.SCHEMA);
}
