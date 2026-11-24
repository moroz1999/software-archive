package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.Changes;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds software, as its form does. */
@Component
@RequiredArgsConstructor
public class CreateSoftwareTool implements WriteTool<CreateSoftwareArguments> {

    private final CreateSoftwareHandler handler;
    private final SoftwareWebMapper mapper;

    @Override
    public String name() {
        return "create_software";
    }

    @Override
    public String description() {
        return "Adds software: its title, platform, and of the rest what is known. Its releases are added after it,"
                + " with create_release.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "title": {"type": "string"},
                    "platform": {"type": "string", "description": "The code of its platform, of list_platforms."},
                    %s
                  },
                  "required": ["title", "platform"],
                  "additionalProperties": false
                }
                """.formatted(SoftwareSchemas.SOFTWARE);
    }

    @Override
    public Class<CreateSoftwareArguments> argumentsType() {
        return CreateSoftwareArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "software";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateSoftwareArguments arguments) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateSoftwareArguments a) {
        SoftwareRequestDto request = new SoftwareRequestDto(
                a.title(),
                Changes.text(a.description()),
                Changes.text(a.releaseDate()),
                a.platform(),
                a.hardware(),
                a.categoryIds(),
                SoftwareForms.credits(a.credits()),
                a.preservation());
        return new Written(
                handler.handle(mapper.toCreate(context.actor(), request)).value());
    }
}
