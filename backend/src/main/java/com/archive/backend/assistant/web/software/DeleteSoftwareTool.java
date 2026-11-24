package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.entry.GetSoftwareTool;
import com.archive.backend.assistant.web.entry.IdArguments;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.domain.SoftwareId;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deletes software with its releases, their files and screenshots; the plan shows them as get_software does. */
@Component
@RequiredArgsConstructor
public class DeleteSoftwareTool implements WriteTool<IdArguments> {

    private final GetSoftwareTool entry;
    private final DeleteSoftwareHandler handler;

    @Override
    public String name() {
        return "delete_software";
    }

    @Override
    public String description() {
        return "Deletes software together with all its releases, their archives and screenshots.";
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
        return "software";
    }

    @Override
    public Object state(ToolContext context, IdArguments arguments) {
        return entry.read(context, arguments);
    }

    @Override
    public Written write(ToolContext context, IdArguments arguments) {
        handler.handle(new DeleteSoftwareCommand(context.actor(), new SoftwareId(arguments.id())));
        return Written.of(arguments.id());
    }
}
