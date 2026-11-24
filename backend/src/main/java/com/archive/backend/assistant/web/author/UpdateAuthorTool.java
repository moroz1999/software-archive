package com.archive.backend.assistant.web.author;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.WriteTools;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.author.app.UpdateAuthorHandler;
import com.archive.backend.author.web.dto.AuthorRequestDto;
import com.archive.backend.author.web.mapper.AuthorWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Changes the fields of a person given; the rest stays. */
@Component
@RequiredArgsConstructor
public class UpdateAuthorTool implements WriteTool<UpdateAuthorArguments> {

    private final AuthorForms forms;
    private final UpdateAuthorHandler handler;
    private final AuthorWebMapper mapper;

    @Override
    public String name() {
        return "update_author";
    }

    @Override
    public String description() {
        return "Changes the fields of a person given; a field left out stays, an empty text clears it, a list given replaces"
                + " the whole list: a label left out of labels is no longer theirs.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid"},
                    "realName": {"type": ["string", "null"]},
                    "notes": {"type": ["string", "null"]},
                    "labels": %s,
                    "locations": %s
                  },
                  "required": ["id"],
                  "additionalProperties": false
                }
                """.formatted(AuthorForms.LABELS, WriteTools.LOCATIONS);
    }

    @Override
    public Class<UpdateAuthorArguments> argumentsType() {
        return UpdateAuthorArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UPDATE;
    }

    @Override
    public String entityType() {
        return "author";
    }

    @Override
    public AuthorState state(ToolContext context, UpdateAuthorArguments a) {
        return forms.author(context, a.id());
    }

    @Override
    public Written write(ToolContext context, UpdateAuthorArguments a) {
        AuthorState current = state(context, a);
        AuthorRequestDto request = AuthorForms.request(
                Changes.text(a.realName(), current.realName()),
                Changes.text(a.notes(), current.notes()),
                Changes.given(a.labels(), current.labels()),
                Changes.given(a.locations(), current.locations()));
        handler.handle(mapper.toUpdate(context.actor(), a.id(), request));
        return new Written(a.id());
    }
}
