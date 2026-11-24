package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.web.dto.SoftwareRequestDto.CreditDto;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Credits somebody on a release, or gives the one credited other roles; the other credits stay. */
@Component
@RequiredArgsConstructor
public class SetReleaseCreditTool implements WriteTool<ReleaseCreditArguments> {

    private final SoftwareForms forms;
    private final UpdateReleaseHandler handler;
    private final ReleaseWebMapper mapper;

    @Override
    public String name() {
        return "set_release_credit";
    }

    @Override
    public String description() {
        return "Credits a person, a group or a label on a release with the roles given, or, credited already, gives them"
                + " these roles instead; everyone else credited stays.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    %s
                    %s
                  },
                  "required": ["softwareId", "releaseId", "id", "type"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.IDS, CreditArguments.PROPERTIES);
    }

    @Override
    public Class<ReleaseCreditArguments> argumentsType() {
        return ReleaseCreditArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.LINK;
    }

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public ReleaseState state(ToolContext context, ReleaseCreditArguments a) {
        return forms.release(context, a.softwareId(), a.releaseId());
    }

    @Override
    public Written write(ToolContext context, ReleaseCreditArguments a) {
        ReleaseState current = state(context, a);
        CreditDto credit = new CreditDto(a.id(), a.type(), a.roles());
        handler.handle(mapper.toUpdate(
                context.actor(),
                a.softwareId(),
                a.releaseId(),
                SoftwareForms.request(current, SoftwareForms.withCredit(current.credits(), credit))));
        return Written.of(a.releaseId());
    }
}
