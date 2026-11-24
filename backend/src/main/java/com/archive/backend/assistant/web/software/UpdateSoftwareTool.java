package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Changes the fields of software given; the rest stays. */
@Component
@RequiredArgsConstructor
public class UpdateSoftwareTool implements WriteTool<UpdateSoftwareArguments> {

    private final SoftwareForms forms;
    private final UpdateSoftwareHandler handler;
    private final SoftwareWebMapper mapper;

    @Override
    public String name() {
        return "update_software";
    }

    @Override
    public String description() {
        return "Changes the fields of software given; a field left out stays, an empty text clears it, a list given"
                + " replaces the whole list. To credit one more person or take one away, set_software_credit and"
                + " remove_software_credit.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "id": {"type": "string", "format": "uuid"},
                    "title": {"type": ["string", "null"]},
                    "platform": {"type": ["string", "null"], "description": "The code of its platform."},
                    %s
                  },
                  "required": ["id"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.SOFTWARE);
    }

    @Override
    public Class<UpdateSoftwareArguments> argumentsType() {
        return UpdateSoftwareArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UPDATE;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public SoftwareState state(ToolContext context, UpdateSoftwareArguments arguments) {
        return forms.software(context, arguments.id());
    }

    @Override
    public Written write(ToolContext context, UpdateSoftwareArguments a) {
        SoftwareState current = state(context, a);
        SoftwareRequestDto request = new SoftwareRequestDto(
                Changes.given(a.title(), current.title()),
                Changes.text(a.description(), current.description()),
                Changes.text(a.releaseDate(), current.releaseDate()),
                Changes.given(a.platform(), current.platform()),
                Changes.given(a.hardware(), current.hardware()),
                Changes.given(a.categoryIds(), current.categoryIds()),
                Changes.given(SoftwareForms.credits(a.credits()), SoftwareForms.currentCredits(current.credits())),
                Changes.given(a.preservation(), current.preservation()));
        handler.handle(mapper.toUpdate(context.actor(), a.id(), request));
        return Written.of(a.id());
    }
}
