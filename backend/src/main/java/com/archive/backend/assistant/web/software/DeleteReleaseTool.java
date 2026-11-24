package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.entry.GetReleaseArguments;
import com.archive.backend.assistant.web.entry.GetReleaseTool;
import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.DeleteReleaseHandler;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Deletes a release with its archive and screenshots; the plan shows it as get_release does. */
@Component
@RequiredArgsConstructor
public class DeleteReleaseTool implements WriteTool<GetReleaseArguments> {

    private final GetReleaseTool entry;
    private final DeleteReleaseHandler handler;
    private final ReleaseWebMapper mapper;

    @Override
    public String name() {
        return "delete_release";
    }

    @Override
    public String description() {
        return "Deletes a release of software together with its archive and screenshots.";
    }

    @Override
    public String schema() {
        return GetReleaseArguments.SCHEMA;
    }

    @Override
    public Class<GetReleaseArguments> argumentsType() {
        return GetReleaseArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.DELETE;
    }

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public Object state(ToolContext context, GetReleaseArguments a) {
        return entry.read(context, a);
    }

    @Override
    public Written write(ToolContext context, GetReleaseArguments a) {
        handler.handle(mapper.toDelete(context.actor(), a.softwareId(), a.releaseId()));
        return Written.of(a.releaseId());
    }
}
