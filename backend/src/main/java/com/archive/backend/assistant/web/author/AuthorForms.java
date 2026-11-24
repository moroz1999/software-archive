package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.tool.LocationArguments;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.author.app.GetAuthorHandler;
import com.archive.backend.author.app.GetAuthorQuery;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.web.dto.AuthorRequestDto;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** A person as their form has them: read as they are, and sent back whole with the fields a tool changes. */
@Component
@RequiredArgsConstructor
class AuthorForms {

    static final String LABELS = """
            {
              "type": ["array", "null"],
              "description": "Every label of the person; given, they replace them: a label left out is no longer\
             theirs.",
              "items": {
                "type": "object",
                "properties": {
                  "id": {"type": ["string", "null"], "format": "uuid", "description": "A label they have; none: a new\
             one."},
                  "name": {"type": ["string", "null"], "description": "Its name; of a label they have, a new name."},
                  "display": {"type": ["boolean", "null"], "description": "The name they are shown by; one of them."}
                },
                "additionalProperties": false
              }
            }""";

    private final GetAuthorHandler author;

    AuthorState author(ToolContext context, UUID id) {
        AuthorDetails details =
                author.handle(new GetAuthorQuery(context.actor(), new AuthorId(id), context.language()));
        return new AuthorState(
                details.realName(),
                details.notes(),
                details.labels().stream()
                        .map(label -> new AuthorLabelArguments(label.id(), label.name(), label.display()))
                        .toList(),
                details.locations().stream()
                        .map(location ->
                                new LocationArguments(location.countryId(), location.cityId(), location.primary()))
                        .toList());
    }

    static AuthorRequestDto request(
            @Nullable String realName,
            @Nullable String notes,
            @Nullable List<AuthorLabelArguments> labels,
            @Nullable List<LocationArguments> locations) {
        return new AuthorRequestDto(
                realName,
                notes,
                labels == null
                        ? null
                        : labels.stream()
                                .map(label -> new AuthorRequestDto.LabelDto(label.id(), label.name(), label.display()))
                                .toList(),
                locations == null
                        ? null
                        : locations.stream()
                                .map(location -> new AuthorRequestDto.LocationDto(
                                        location.countryId(), location.cityId(), location.primary()))
                                .toList());
    }
}
