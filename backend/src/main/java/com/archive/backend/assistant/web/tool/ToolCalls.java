package com.archive.backend.assistant.web.tool;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/** Runs a call of a read tool given by its name and its JSON arguments, as the model writes it. */
@Component
@RequiredArgsConstructor
public class ToolCalls {

    private final ToolRegistry registry;
    private final ToolArguments arguments;

    public Object read(String name, String json, ToolContext context) {
        if (!(registry.tool(name) instanceof ReadTool<?> tool)) {
            throw new ToolArgumentsException(name + " changes the archive: put it into a plan instead.");
        }
        return read(tool, json, context);
    }

    private <A extends Record> Object read(ReadTool<A> tool, String json, ToolContext context) {
        return tool.read(context, arguments.parse(tool, json));
    }
}
