package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.label.app.MergeLabelIntoAuthorCommand;
import com.archive.backend.label.app.MergeLabelIntoAuthorHandler;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Merges a label into the person it is a duplicate of. */
@Component
@RequiredArgsConstructor
public class MergeLabelIntoAuthorTool implements WriteTool<MergeLabelIntoAuthorArguments> {

    private final GetLabelTool entry;
    private final MergeLabelIntoAuthorHandler handler;

    @Override
    public String name() {
        return "merge_label_into_author";
    }

    @Override
    public String description() {
        return "Merges a label into the person it is a duplicate of: its credits and memberships go to the person, the label"
                + " is deleted.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The label."},
                    "authorId": {"type": "string", "format": "uuid", "description": "The person."}
                  },
                  "required": ["id", "authorId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<MergeLabelIntoAuthorArguments> argumentsType() {
        return MergeLabelIntoAuthorArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.MERGE;
    }

    @Override
    public String entityType() {
        return "label";
    }

    @Override
    public Object state(ToolContext context, MergeLabelIntoAuthorArguments a) {
        return entry.read(context, new IdArguments(a.id()));
    }

    @Override
    public Written write(ToolContext context, MergeLabelIntoAuthorArguments a) {
        handler.handle(
                new MergeLabelIntoAuthorCommand(context.actor(), new LabelId(a.id()), new AuthorId(a.authorId())));
        return new Written(a.authorId());
    }
}
