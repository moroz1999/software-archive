package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Takes a credit away from a release; the other credits stay. */
@Component
@RequiredArgsConstructor
public class RemoveReleaseCreditTool implements WriteTool<RemoveReleaseCreditArguments> {

    private final SoftwareForms forms;
    private final UpdateReleaseHandler handler;
    private final ReleaseWebMapper mapper;

    @Override
    public String name() {
        return "remove_release_credit";
    }

    @Override
    public String description() {
        return "Takes the credit of a person, a group or a label away from a release; everyone else credited stays.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    %s
                    "id": {"type": "string", "format": "uuid", "description": "The one credited."}
                  },
                  "required": ["softwareId", "releaseId", "id"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.IDS);
    }

    @Override
    public Class<RemoveReleaseCreditArguments> argumentsType() {
        return RemoveReleaseCreditArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UNLINK;
    }

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public ReleaseState state(ToolContext context, RemoveReleaseCreditArguments a) {
        ReleaseState current = forms.release(context, a.softwareId(), a.releaseId());
        SoftwareForms.requireCredited(current.credits(), a.id());
        return current;
    }

    @Override
    public Written write(ToolContext context, RemoveReleaseCreditArguments a) {
        ReleaseState current = forms.release(context, a.softwareId(), a.releaseId());
        handler.handle(mapper.toUpdate(
                context.actor(),
                a.softwareId(),
                a.releaseId(),
                SoftwareForms.request(current, SoftwareForms.withoutCredit(current.credits(), a.id()))));
        return Written.of(a.releaseId());
    }
}
