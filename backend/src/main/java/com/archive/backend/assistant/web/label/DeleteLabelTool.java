package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.entry.GetLabelTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.label.app.DeleteLabelCommand;
import com.archive.backend.label.app.DeleteLabelHandler;
import com.archive.backend.label.domain.LabelId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deletes a label; the plan shows it as get_label does. */
@Component
@RequiredArgsConstructor
public class DeleteLabelTool implements WriteTool<IdArguments> {

    private final GetLabelTool entry;
    private final DeleteLabelHandler handler;

    @Override
    public String name() {
        return "delete_label";
    }

    @Override
    public String description() {
        return "Deletes a label, with its credits. The display name of a person cannot be deleted.";
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
        return ChangeKind.DELETE;
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
        handler.handle(new DeleteLabelCommand(context.actor(), new LabelId(a.id())));
        return Written.of(a.id());
    }
}
