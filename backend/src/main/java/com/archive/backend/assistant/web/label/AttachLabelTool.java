package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.author.app.AttachLabelToAuthorCommand;
import com.archive.backend.author.app.AttachLabelToAuthorHandler;
import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.app.AttachLabelToGroupCommand;
import com.archive.backend.group.app.AttachLabelToGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelAttachment;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Attaches a label of nobody to the person or the group it is found to be of. */
@Component
@RequiredArgsConstructor
public class AttachLabelTool implements WriteTool<AttachLabelArguments> {

    private final GetLabelTool entry;
    private final AttachLabelToAuthorHandler author;
    private final AttachLabelToGroupHandler group;

    @Override
    public String name() {
        return "attach_label";
    }

    @Override
    public String description() {
        return "Attaches a label of nobody to the person or the group it is found to be of, with its credits.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The label of nobody."},
                    "ownerType": {"enum": ["author", "group"]},
                    "ownerId": {"type": "string", "format": "uuid"},
                    "display": {"type": ["boolean", "null"],
                     "description": "Of a person: whether it becomes the name they are shown by."}
                  },
                  "required": ["id", "ownerType", "ownerId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<AttachLabelArguments> argumentsType() {
        return AttachLabelArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.ATTACH;
    }

    @Override
    public String entityType() {
        return "label";
    }

    @Override
    public Object state(ToolContext context, AttachLabelArguments a) {
        return entry.read(context, new IdArguments(a.id()));
    }

    @Override
    public Written write(ToolContext context, AttachLabelArguments a) {
        LabelId label = new LabelId(a.id());
        if (a.ownerType().equals("group")) {
            group.handle(new AttachLabelToGroupCommand(context.actor(), label, new GroupId(a.ownerId())));
        } else {
            author.handle(new AttachLabelToAuthorCommand(
                    context.actor(),
                    new LabelAttachment(label, Boolean.TRUE.equals(a.display())),
                    new AuthorId(a.ownerId())));
        }
        return Written.of(a.id());
    }
}
