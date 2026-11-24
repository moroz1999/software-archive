package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.WriteTools;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.author.app.CreateAuthorHandler;
import com.archive.backend.author.web.dto.AuthorRequestDto;
import com.archive.backend.author.web.mapper.AuthorWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds a person with their labels and locations, as the person form does. */
@Component
@RequiredArgsConstructor
public class CreateAuthorTool implements WriteTool<CreateAuthorArguments> {

    private final CreateAuthorHandler handler;
    private final AuthorWebMapper mapper;

    @Override
    public String name() {
        return "create_author";
    }

    @Override
    public String description() {
        return "Adds a person: their real name, notes, labels (one of them the display name) and locations. Look for them"
                + " first: equal names are not the same person.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "realName": {"type": ["string", "null"]},
                    "notes": {"type": ["string", "null"]},
                    "labels": %s,
                    "locations": %s
                  },
                  "additionalProperties": false
                }
                """.formatted(AuthorForms.LABELS, WriteTools.LOCATIONS);
    }

    @Override
    public Class<CreateAuthorArguments> argumentsType() {
        return CreateAuthorArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "author";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateAuthorArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateAuthorArguments a) {
        AuthorRequestDto request =
                AuthorForms.request(Changes.text(a.realName()), Changes.text(a.notes()), a.labels(), a.locations());
        return new Written(
                handler.handle(mapper.toCreate(context.actor(), request)).value());
    }
}
