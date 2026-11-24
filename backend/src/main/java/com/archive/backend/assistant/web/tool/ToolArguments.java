package com.archive.backend.assistant.web.tool;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.networknt.schema.JsonSchema;
import com.networknt.schema.JsonSchemaFactory;
import com.networknt.schema.PathType;
import com.networknt.schema.SchemaValidatorsConfig;
import com.networknt.schema.SpecVersion;
import com.networknt.schema.ValidationMessage;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import org.springframework.stereotype.Component;

/**
 * Reads the JSON arguments of a call into the tool's arguments record, after checking them against the tool's schema.
 * A field not required may be left out or given as {@code null}: both read as not given.
 */
@Component
public class ToolArguments {

    private static final JsonSchemaFactory SCHEMAS = JsonSchemaFactory.getInstance(SpecVersion.VersionFlag.V202012);
    private static final SchemaValidatorsConfig CONFIG = SchemaValidatorsConfig.builder()
            .formatAssertionsEnabled(true)
            .pathType(PathType.JSON_PATH)
            .build();

    private final ObjectMapper json;
    private final Map<String, JsonSchema> schemas = new ConcurrentHashMap<>();

    public ToolArguments(ObjectMapper json) {
        this.json = json;
    }

    public <A extends Record> A parse(Tool<A> tool, String arguments) {
        JsonNode node = tree(arguments);
        if (!node.isObject()) {
            throw new ToolArgumentsException("The arguments must be a JSON object.");
        }
        Set<ValidationMessage> problems = schema(tool).validate(node);
        if (!problems.isEmpty()) {
            throw new ToolArgumentsException(problems.stream()
                    .map(ValidationMessage::getMessage)
                    .sorted()
                    .collect(Collectors.joining("; ")));
        }
        try {
            return json.treeToValue(node, tool.argumentsType());
        } catch (JsonProcessingException e) {
            throw new ToolArgumentsException("The arguments cannot be read: " + e.getOriginalMessage());
        }
    }

    private JsonNode tree(String arguments) {
        try {
            return json.readTree(arguments);
        } catch (JsonProcessingException e) {
            throw new ToolArgumentsException("The arguments are not JSON: " + e.getOriginalMessage());
        }
    }

    private JsonSchema schema(Tool<?> tool) {
        return schemas.computeIfAbsent(tool.schema(), schema -> SCHEMAS.getSchema(schema, CONFIG));
    }
}
