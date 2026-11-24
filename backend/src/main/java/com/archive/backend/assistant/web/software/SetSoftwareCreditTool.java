package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.web.dto.SoftwareRequestDto.CreditDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Credits somebody on software, or gives the one credited other roles; the other credits stay. */
@Component
@RequiredArgsConstructor
public class SetSoftwareCreditTool implements WriteTool<SoftwareCreditArguments> {

    private final SoftwareForms forms;
    private final UpdateSoftwareHandler handler;
    private final SoftwareWebMapper mapper;

    @Override
    public String name() {
        return "set_software_credit";
    }

    @Override
    public String description() {
        return "Credits a person, a group or a label on software with the roles given, or, credited already, gives them"
                + " these roles instead; everyone else credited stays.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "softwareId": {"type": "string", "format": "uuid"},
                    %s
                  },
                  "required": ["softwareId", "id", "type"],
                  "additionalProperties": false
                }
                """.formatted(CreditArguments.PROPERTIES);
    }

    @Override
    public Class<SoftwareCreditArguments> argumentsType() {
        return SoftwareCreditArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.LINK;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public SoftwareState state(ToolContext context, SoftwareCreditArguments arguments) {
        return forms.software(context, arguments.softwareId());
    }

    @Override
    public Written write(ToolContext context, SoftwareCreditArguments a) {
        SoftwareState current = state(context, a);
        CreditDto credit = new CreditDto(a.id(), a.type(), a.roles());
        handler.handle(mapper.toUpdate(
                context.actor(),
                a.softwareId(),
                SoftwareForms.request(current, SoftwareForms.withCredit(current.credits(), credit))));
        return new Written(a.softwareId());
    }
}
