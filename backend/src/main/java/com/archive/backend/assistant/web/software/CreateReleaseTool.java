package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.domain.ReleaseType;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds a release of software, as the release form does, without its archive. */
@Component
@RequiredArgsConstructor
public class CreateReleaseTool implements WriteTool<CreateReleaseArguments> {

    private final CreateReleaseHandler handler;
    private final ReleaseWebMapper mapper;

    @Override
    public String name() {
        return "create_release";
    }

    @Override
    public String description() {
        return "Adds a release of software: its type (original by default) and of the rest what is known. Its archive is not"
                + " added with it.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "softwareId": {"type": "string", "format": "uuid"},
                    "type": {"type": ["string", "null"], "enum": %s},
                    %s
                  },
                  "required": ["softwareId"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.RELEASE_TYPES.replace("]", ", null]"), SoftwareSchemas.RELEASE);
    }

    @Override
    public Class<CreateReleaseArguments> argumentsType() {
        return CreateReleaseArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "release";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateReleaseArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateReleaseArguments a) {
        ReleaseRequestDto request = new ReleaseRequestDto(
                Changes.text(a.title()),
                Changes.text(a.version()),
                a.type() == null ? ReleaseType.ORIGINAL.code() : a.type(),
                Changes.text(a.releaseDate()),
                Changes.text(a.description()),
                a.hardware(),
                SoftwareForms.credits(a.credits()));
        return new Written(handler.handle(mapper.toCreate(context.actor(), a.softwareId(), request))
                .value());
    }
}
