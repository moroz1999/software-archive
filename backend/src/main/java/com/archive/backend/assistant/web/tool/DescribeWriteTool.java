package com.archive.backend.assistant.web.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.springframework.stereotype.Component;

/**
 * What a write tool does and the schema of its arguments, read by the model before it puts the tool into a plan: the
 * descriptions and schemas of every write tool do not ride along with each request.
 */
@Component
public class DescribeWriteTool implements ReadTool<DescribeWriteToolArguments> {

    /** Every write tool; not the registry, which holds this tool too. */
    private final List<WriteTool<?>> writeTools;

    private final ObjectMapper json;

    public DescribeWriteTool(List<WriteTool<?>> writeTools, ObjectMapper json) {
        this.writeTools = writeTools;
        this.json = json;
    }

    @Override
    public String name() {
        return "describe_write_tool";
    }

    @Override
    public String description() {
        return "What a tool that changes the archive does, what kind of change it makes to which entry, and the JSON"
                + " Schema of its arguments. Read it before you put the tool into a plan. The tools: "
                + String.join(", ", names()) + ".";
    }

    @Override
    public String schema() {
        return """
                {
                  "type": "object",
                  "properties": {"name": {"type": "string", "description": "The name of the write tool."}},
                  "required": ["name"],
                  "additionalProperties": false
                }
                """;
    }

    @Override
    public Class<DescribeWriteToolArguments> argumentsType() {
        return DescribeWriteToolArguments.class;
    }

    @Override
    public Object read(ToolContext context, DescribeWriteToolArguments arguments) {
        WriteTool<?> tool = writeTools.stream()
                .filter(candidate -> candidate.name().equals(arguments.name()))
                .findFirst()
                .orElseThrow(() -> new ToolArgumentsException("No write tool is named " + arguments.name()
                        + ". The write tools: " + String.join(", ", names()) + "."));
        try {
            return new WriteToolDescription(
                    tool.name(),
                    tool.description(),
                    tool.kind().code(),
                    tool.entityType(),
                    json.readTree(tool.schema()));
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("The schema of " + tool.name() + " is not JSON", e);
        }
    }

    private List<String> names() {
        return writeTools.stream().map(Tool::name).toList();
    }
}
