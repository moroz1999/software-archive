package com.archive.backend.assistant.web.catalog;

import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.hardware.app.ListHardwareHandler;
import com.archive.backend.hardware.app.ListHardwareQuery;
import com.archive.backend.hardware.domain.PlatformCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** The hardware of a platform. */
@Component
@RequiredArgsConstructor
public class ListHardwareTool implements ReadTool<ListHardwareArguments> {

    private final ListHardwareHandler handler;

    @Override
    public String name() {
        return "list_hardware";
    }

    @Override
    public String description() {
        return "The hardware of a platform with codes, names and groups (computers, storage, sound…): what software and"
                + " releases of that platform can state.";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"platform": {"type": "string", "description": "The platform code."}},
                  "required": ["platform"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<ListHardwareArguments> argumentsType() {
        return ListHardwareArguments.class;
    }

    @Override
    public Object read(ToolContext context, ListHardwareArguments arguments) {
        return handler.handle(new ListHardwareQuery(new PlatformCode(arguments.platform()), context.language()));
    }
}
