package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ToolSchemas;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseType;
import java.util.Arrays;

/** The fields of software and of a release, as the schemas of their tools have them. */
final class SoftwareSchemas {

    static final String DATE = """
            {"type": ["string", "null"], "pattern": "^(\\\\d{4}(-\\\\d{2}(-\\\\d{2})?)?)?$",\
             "description": "A year, 1987, or a day, 1987-05-12; empty: unknown."}""";

    static final String PRESERVATION = """
            {"type": ["string", "null"], "enum": %s, "description": "Whether the software is preserved."}""".formatted(ToolSchemas.strings(Arrays.stream(PreservationStatus.values())
                    .map(PreservationStatus::code)
                    .toList())
            .replace("]", ", null]"));

    static final String RELEASE_TYPES = ToolSchemas.strings(
            Arrays.stream(ReleaseType.values()).map(ReleaseType::code).toList());

    /** The fields of software besides its title and its platform, which a create requires. */
    static final String SOFTWARE = """
            "description": {"type": ["string", "null"], "description": "Empty: none."},
            "releaseDate": %s,
            "hardware": {"type": ["array", "null"], "items": {"type": "string"},\
             "description": "The codes of its base hardware, of list_hardware; given, they replace it."},
            "categoryIds": {"type": ["array", "null"], "items": {"type": "string", "format": "uuid"},\
             "description": "The categories it is in, of list_categories; given, they replace them."},
            "credits": %s,
            "preservation": %s
            """.formatted(DATE, CreditArguments.SCHEMA, PRESERVATION);

    /** The fields of a release besides its type, which a create does not require either: original by default. */
    static final String RELEASE = """
            "title": {"type": ["string", "null"], "description": "Its own title, when it is not the software's; empty:\
             none."},
            "version": {"type": ["string", "null"], "description": "Empty: none."},
            "releaseDate": %s,
            "description": {"type": ["string", "null"], "description": "Empty: none."},
            "hardware": {"type": ["array", "null"], "items": {"type": "string"},\
             "description": "The codes of its own hardware, of list_hardware, only where it differs from the\
             software's in a group: a group it names replaces that group of the software's; given, they replace it."},
            "credits": %s
            """.formatted(DATE, CreditArguments.SCHEMA);

    /** The ids of a release: of its software and its own, as properties of a schema. */
    static final String IDS = "\"softwareId\": {\"type\": \"string\", \"format\": \"uuid\"},"
            + " \"releaseId\": {\"type\": \"string\", \"format\": \"uuid\"},";

    private SoftwareSchemas() {}
}
