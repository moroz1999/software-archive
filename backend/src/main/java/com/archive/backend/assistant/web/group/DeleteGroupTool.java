package com.archive.backend.assistant.web.group;

import com.archive.backend.assistant.web.entry.GetGroupTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.group.app.DeleteGroupCommand;
import com.archive.backend.group.app.DeleteGroupHandler;
import com.archive.backend.group.domain.GroupId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deletes a group, leaving its labels to nobody; the plan shows it as get_group does. */
@Component
@RequiredArgsConstructor
public class DeleteGroupTool implements WriteTool<IdArguments> {

    private final GetGroupTool entry;
    private final DeleteGroupHandler handler;

    @Override
    public String name() {
        return "delete_group";
    }

    @Override
    public String description() {
        return "Deletes a group; its labels stay, as labels of nobody, with their credits.";
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
        return "group";
    }

    @Override
    public Object state(ToolContext context, IdArguments a) {
        return entry.read(context, a);
    }

    @Override
    public Written write(ToolContext context, IdArguments a) {
        handler.handle(new DeleteGroupCommand(context.actor(), new GroupId(a.id())));
        return Written.of(a.id());
    }
}
