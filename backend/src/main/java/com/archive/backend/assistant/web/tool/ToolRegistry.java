package com.archive.backend.assistant.web.tool;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.springframework.stereotype.Component;

/** Every tool of the assistant by its name, in the order they are offered to the model. */
@Component
public class ToolRegistry {

    private final Map<String, Tool<?>> tools = new LinkedHashMap<>();

    public ToolRegistry(List<Tool<?>> tools) {
        for (Tool<?> tool : tools) {
            if (this.tools.putIfAbsent(tool.name(), tool) != null) {
                throw new IllegalStateException("Two tools are named " + tool.name());
            }
        }
    }

    public Tool<?> tool(String name) {
        Tool<?> tool = tools.get(name);
        if (tool == null) {
            throw new UnknownToolException(name);
        }
        return tool;
    }

    public List<Tool<?>> tools() {
        return List.copyOf(tools.values());
    }

    public List<WriteTool<?>> writeTools() {
        return tools.values().stream()
                .filter(WriteTool.class::isInstance)
                .<WriteTool<?>>map(tool -> (WriteTool<?>) tool)
                .toList();
    }

    public List<ReadTool<?>> readTools() {
        return tools.values().stream()
                .filter(ReadTool.class::isInstance)
                .<ReadTool<?>>map(tool -> (ReadTool<?>) tool)
                .toList();
    }
}
