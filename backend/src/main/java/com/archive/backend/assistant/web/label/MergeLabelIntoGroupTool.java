package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.app.MergeLabelIntoGroupCommand;
import com.archive.backend.label.app.MergeLabelIntoGroupHandler;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Merges a label into the group it is a duplicate of. */
@Component
@RequiredArgsConstructor
public class MergeLabelIntoGroupTool implements WriteTool<LabelGroupArguments> {

    private final GetLabelTool entry;
    private final MergeLabelIntoGroupHandler handler;

    @Override
    public String name() {
        return "merge_label_into_group";
    }

    @Override
    public String description() {
        return "Merges a label into the group it is a duplicate of: its credits go to the group, the label is deleted. Only a label of nobody or of a group is merged, never a label of a person.";
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
        return ChangeKind.MERGE;
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
        handler.handle(new MergeLabelIntoGroupCommand(context.actor(), new LabelId(a.id()), new GroupId(a.groupId())));
        return Written.of(a.groupId());
    }
}
