package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.WriteTools;
import com.archive.backend.assistant.web.tool.Written;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Renames or relocates a label; whose it is stays. */
@Component
@RequiredArgsConstructor
public class UpdateLabelTool implements WriteTool<UpdateLabelArguments> {

    private final LabelForms forms;

    @Override
    public String name() {
        return "update_label";
    }

    @Override
    public String description() {
        return "Changes the name or the locations of a label given; the rest stays, and so does whose it is: attach_label and"
                + " detach_label change that.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid"},
                    "name": {"type": ["string", "null"]},
                    "locations": %s
                  },
                  "required": ["id"],
                  "additionalProperties": false
                }
                """.formatted(WriteTools.LOCATIONS);
    }

    @Override
    public Class<UpdateLabelArguments> argumentsType() {
        return UpdateLabelArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UPDATE;
    }

    @Override
    public String entityType() {
        return "label";
    }

    @Override
    public LabelState state(ToolContext context, UpdateLabelArguments a) {
        return forms.label(context, a.id());
    }

    @Override
    public Written write(ToolContext context, UpdateLabelArguments a) {
        LabelState current = state(context, a);
        forms.edit(
                context,
                a.id(),
                Changes.given(a.name(), current.name()),
                Changes.given(a.locations(), current.locations()),
                true);
        return new Written(a.id());
    }
}
