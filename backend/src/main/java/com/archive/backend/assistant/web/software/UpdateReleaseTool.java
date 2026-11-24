package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Changes the fields of a release given; the rest, its archive among them, stays. */
@Component
@RequiredArgsConstructor
public class UpdateReleaseTool implements WriteTool<UpdateReleaseArguments> {

    private final SoftwareForms forms;
    private final UpdateReleaseHandler handler;
    private final ReleaseWebMapper mapper;

    @Override
    public String name() {
        return "update_release";
    }

    @Override
    public String description() {
        return "Changes the fields of a release given; a field left out stays, an empty text clears it, a list given replaces"
                + " the whole list. Its archive stays.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    %s
                    "type": {"type": ["string", "null"], "enum": %s},
                    %s
                  },
                  "required": ["softwareId", "releaseId"],
                  "additionalProperties": false
                }
                """.formatted(
                SoftwareSchemas.IDS, SoftwareSchemas.RELEASE_TYPES.replace("]", ", null]"), SoftwareSchemas.RELEASE);
    }

    @Override
    public Class<UpdateReleaseArguments> argumentsType() {
        return UpdateReleaseArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.UPDATE;
    }

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public ReleaseState state(ToolContext context, UpdateReleaseArguments a) {
        return forms.release(context, a.softwareId(), a.releaseId());
    }

    @Override
    public Written write(ToolContext context, UpdateReleaseArguments a) {
        ReleaseState current = state(context, a);
        ReleaseRequestDto request = new ReleaseRequestDto(
                Changes.text(a.title(), current.title()),
                Changes.text(a.version(), current.version()),
                Changes.given(a.type(), current.type()),
                Changes.text(a.releaseDate(), current.releaseDate()),
                Changes.text(a.description(), current.description()),
                Changes.given(a.hardware(), current.hardware()),
                Changes.given(SoftwareForms.credits(a.credits()), SoftwareForms.currentCredits(current.credits())));
        handler.handle(mapper.toUpdate(context.actor(), a.softwareId(), a.releaseId(), request));
        return new Written(a.releaseId());
    }
}
