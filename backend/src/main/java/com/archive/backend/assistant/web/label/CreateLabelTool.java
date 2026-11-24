package com.archive.backend.assistant.web.label;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.WriteTools;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.label.app.CreateLabelHandler;
import com.archive.backend.label.web.dto.NewLabelRequestDto;
import com.archive.backend.label.web.mapper.LabelWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds a label of nobody, as the form of a new label does. */
@Component
@RequiredArgsConstructor
public class CreateLabelTool implements WriteTool<CreateLabelArguments> {

    private final CreateLabelHandler handler;
    private final LabelWebMapper mapper;

    @Override
    public String name() {
        return "create_label";
    }

    @Override
    public String description() {
        return "Adds a label of nobody: a name somebody is credited under, before it is known whose it is. A label of a"
                + " person or a group is added with them, in create_author and create_group.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "name": {"type": "string"},
                    "locations": %s
                  },
                  "required": ["name"],
                  "additionalProperties": false
                }
                """.formatted(WriteTools.LOCATIONS);
    }

    @Override
    public Class<CreateLabelArguments> argumentsType() {
        return CreateLabelArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "label";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateLabelArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateLabelArguments a) {
        NewLabelRequestDto request = new NewLabelRequestDto(a.name(), LabelForms.locations(a.locations()));
        return Written.of(
                handler.handle(mapper.toCreate(context.actor(), request)).value());
    }
}
