package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ToolSchemas;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareReferenceType;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** A person, a group or the exact label of either, credited with the roles they had. */
public record CreditArguments(
        UUID id, String type, @Nullable List<String> roles) {

    /** Who is credited: a person or a group by themselves, or one of their labels. */
    static final String TYPES = ToolSchemas.strings(Arrays.stream(SoftwareReferenceType.values())
            .map(SoftwareReferenceType::code)
            .toList());

    static final String ROLES = ToolSchemas.strings(SoftwareCreditDraft.ALLOWED_ROLES);

    /** The properties of a credit, to put into the schema of a credit or of a tool. */
    static final String PROPERTIES = """
            "id": {"type": "string", "format": "uuid", "description": "The person, the group or the label credited."},
            "type": {"enum": %s, "description": "author or group credits them by themselves, author-label or\
             group-label under the exact label they used."},
            "roles": {"type": ["array", "null"], "items": {"enum": %s}, "description": "None: unknown."}
            """.formatted(TYPES, ROLES);

    static final String SCHEMA = """
            {
              "type": ["array", "null"],
              "description": "Everyone credited; given, it replaces the credits.",
              "items": {
                "type": "object",
                "properties": {%s},
                "required": ["id", "type"],
                "additionalProperties": false
              }
            }
            """.formatted(PROPERTIES);
}
