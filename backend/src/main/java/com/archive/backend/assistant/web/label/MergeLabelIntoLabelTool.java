package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.label.app.MergeLabelIntoLabelCommand;
import com.archive.backend.label.app.MergeLabelIntoLabelHandler;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Merges a label into another label it is a duplicate of. */
@Component
@RequiredArgsConstructor
public class MergeLabelIntoLabelTool implements WriteTool<MergeLabelIntoLabelArguments> {

    private final GetLabelTool entry;
    private final MergeLabelIntoLabelHandler handler;

    @Override
    public String name() {
        return "merge_label_into_label";
    }

    @Override
    public String description() {
        return "Merges a label into another label it is a duplicate of: its credits go to the other one, the label is"
                + " deleted.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid", "description": "The label."},
                    "labelId": {"type": "string", "format": "uuid", "description": "The label it is merged into."}
                  },
                  "required": ["id", "labelId"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<MergeLabelIntoLabelArguments> argumentsType() {
        return MergeLabelIntoLabelArguments.class;
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
    public Object state(ToolContext context, MergeLabelIntoLabelArguments a) {
        return entry.read(context, new IdArguments(a.id()));
    }

    @Override
    public Written write(ToolContext context, MergeLabelIntoLabelArguments a) {
        handler.handle(new MergeLabelIntoLabelCommand(context.actor(), new LabelId(a.id()), new LabelId(a.labelId())));
        return new Written(a.labelId());
    }
}
