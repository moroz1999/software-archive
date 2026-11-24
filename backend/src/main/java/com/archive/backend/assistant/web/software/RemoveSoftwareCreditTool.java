package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Takes a credit away from software; the other credits stay. */
@Component
@RequiredArgsConstructor
public class RemoveSoftwareCreditTool implements WriteTool<RemoveSoftwareCreditArguments> {

    private final SoftwareForms forms;
    private final UpdateSoftwareHandler handler;
    private final SoftwareWebMapper mapper;

    @Override
    public String name() {
        return "remove_software_credit";
    }

    @Override
    public String description() {
        return "Takes the credit of a person, a group or a label away from software; everyone else credited stays.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "softwareId": {"type": "string", "format": "uuid"},
                    "id": {"type": "string", "format": "uuid", "description": "The one credited."}
                  },
                  "required": ["softwareId", "id"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<RemoveSoftwareCreditArguments> argumentsType() {
        return RemoveSoftwareCreditArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UNLINK;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public SoftwareState state(ToolContext context, RemoveSoftwareCreditArguments a) {
        SoftwareState current = forms.software(context, a.softwareId());
        SoftwareForms.requireCredited(current.credits(), a.id());
        return current;
    }

    @Override
    public Written write(ToolContext context, RemoveSoftwareCreditArguments a) {
        SoftwareState current = forms.software(context, a.softwareId());
        handler.handle(mapper.toUpdate(
                context.actor(),
                a.softwareId(),
                SoftwareForms.request(current, SoftwareForms.withoutCredit(current.credits(), a.id()))));
        return Written.of(a.softwareId());
    }
}
