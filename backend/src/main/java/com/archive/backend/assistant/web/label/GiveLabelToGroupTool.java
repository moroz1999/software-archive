package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.app.GiveLabelToGroupCommand;
import com.archive.backend.label.app.GiveLabelToGroupHandler;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Makes a label a label of a group, as the label page does. */
@Component
@RequiredArgsConstructor
public class GiveLabelToGroupTool implements WriteTool<LabelGroupArguments> {

    private final GetLabelTool entry;
    private final GiveLabelToGroupHandler handler;

    @Override
    public String name() {
        return "give_label_to_group";
    }

    @Override
    public String description() {
        return "Makes a label of a person or of nobody a label of a group: the name the group, not a person, is credited"
                + " under.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The label."},
                    "groupId": {"type": "string", "format": "uuid", "description": "The group."}
                  },
                  "required": ["id", "groupId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<LabelGroupArguments> argumentsType() {
        return LabelGroupArguments.class;
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
    public Object state(ToolContext context, LabelGroupArguments a) {
        return entry.read(context, new IdArguments(a.id()));
    }

    @Override
    public Written write(ToolContext context, LabelGroupArguments a) {
        handler.handle(new GiveLabelToGroupCommand(context.actor(), new LabelId(a.id()), new GroupId(a.groupId())));
        return new Written(a.id());
    }
}
