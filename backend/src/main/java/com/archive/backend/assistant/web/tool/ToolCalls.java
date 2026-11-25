package com.archive.backend.assistant.web.tool;

import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Runs a call of a tool given by its name and its JSON arguments, as the model writes it: a read at once, a write only
 * as a change of an approved plan, and before that the state of what the write changes.
 */
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

    /** What a write would change, as it is now; none for a create. */
    public @Nullable Object state(String name, String json, ToolContext context) {
        return state(writeTool(name), json, context);
    }

    /** That the arguments fit the write tool, without reading what it changes. */
    public void check(String name, String json) {
        arguments.parse(writeTool(name), json);
    }

    public Written write(String name, String json, ToolContext context) {
        return write(writeTool(name), json, context);
    }

    private WriteTool<?> writeTool(String name) {
        if (!(registry.tool(name) instanceof WriteTool<?> tool)) {
            throw new ToolArgumentsException(
                    name + " only reads the archive: call it instead of putting it into a plan.");
        }
        return tool;
    }

    private <A extends Record> Object read(ReadTool<A> tool, String json, ToolContext context) {
        return tool.read(context, arguments.parse(tool, json));
    }

    private <A extends Record> @Nullable Object state(WriteTool<A> tool, String json, ToolContext context) {
        return tool.state(context, arguments.parse(tool, json));
    }

    private <A extends Record> Written write(WriteTool<A> tool, String json, ToolContext context) {
        return tool.write(context, arguments.parse(tool, json));
    }
}
