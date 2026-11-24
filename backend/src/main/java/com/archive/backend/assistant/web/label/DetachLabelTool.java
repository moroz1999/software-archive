package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Takes a label away from its owner, leaving it to nobody. */
@Component
@RequiredArgsConstructor
public class DetachLabelTool implements WriteTool<IdArguments> {

    private final GetLabelTool entry;
    private final LabelForms forms;

    @Override
    public String name() {
        return "detach_label";
    }

    @Override
    public String description() {
        return "Takes a label away from the person or the group it belongs to; it stays, as a label of nobody, with its"
                + " credits.";
    }

    @Override
    public String schema() {
        return IdArguments.SCHEMA;
    }

    @Override
    public Class<IdArguments> argumentsType() {
        return IdArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.DETACH;
    }

    @Override
    public String entityType() {
        return "label";
    }

    @Override
    public Object state(ToolContext context, IdArguments a) {
        return entry.read(context, a);
    }

    @Override
    public Written write(ToolContext context, IdArguments a) {
        LabelState current = forms.label(context, a.id());
        forms.edit(context, a.id(), current.name(), current.locations(), false);
        return new Written(a.id());
    }
}
