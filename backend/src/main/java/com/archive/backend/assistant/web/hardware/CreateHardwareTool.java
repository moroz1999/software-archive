package com.archive.backend.assistant.web.hardware;

import com.archive.backend.assistant.web.tool.ChangeKind;
import com.archive.backend.assistant.web.tool.NamesArguments;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.archive.backend.assistant.web.tool.Written;
import com.archive.backend.hardware.app.CreateHardwareHandler;
import com.archive.backend.hardware.web.dto.HardwareRequestDto;
import com.archive.backend.hardware.web.mapper.HardwareWebMapper;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** Adds an item of hardware, as the management of hardware does. */
@Component
@RequiredArgsConstructor
public class CreateHardwareTool implements WriteTool<CreateHardwareArguments> {

    private final CreateHardwareHandler handler;
    private final HardwareWebMapper mapper;

    @Override
    public String name() {
        return "create_hardware";
    }

    @Override
    public String description() {
        return "Adds an item of hardware the source clearly names and list_hardware lacks: its code, names, group and"
                + " platforms. The result is its code.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {
                    "code": {"type": "string", "pattern": "^[a-z0-9]+(-[a-z0-9]+)*$",
                     "description": "Lower-case Latin letters and digits, words joined by a hyphen; never changes."},
                    "names": %s,
                    "group": {"type": "string", "description": "The code of its group, as list_hardware shows it."},
                    "platforms": {"type": "array", "minItems": 1, "items": {"type": "string"},
                     "description": "The codes of its platforms, of list_platforms."}
                  },
                  "required": ["code", "names", "group", "platforms"],
                  "additionalProperties": false
                }
                """.formatted(NamesArguments.SCHEMA);
    }

    @Override
    public Class<CreateHardwareArguments> argumentsType() {
        return CreateHardwareArguments.class;
    }

    @Override
    public ChangeKind kind() {
        return ChangeKind.CREATE;
    }

    @Override
    public String entityType() {
        return "hardware";
    }

    @Override
    public @Nullable Object state(ToolContext context, CreateHardwareArguments a) {
        return null;
    }

    @Override
    public Written write(ToolContext context, CreateHardwareArguments a) {
        HardwareRequestDto request = new HardwareRequestDto(a.code(), a.names().byLanguage(), a.group(), a.platforms());
        return new Written(
                handler.handle(mapper.toCreate(context.actor(), request)).value());
    }
}
