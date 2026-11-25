package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.domain.chat.PlanAction;
import com.archive.backend.assistant.domain.chat.PlanTitle;
import com.archive.backend.assistant.read.model.ModelTool;
import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolRegistry;
import com.archive.backend.assistant.web.tool.WriteTool;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * {@code propose_plan}: checks a plan the model writes before the editor sees it. Each change must name a write tool;
 * its references must point to earlier changes; its arguments must fit the tool's schema, an id taken from an earlier
 * change counted as one; and a change that takes no such id reads what it changes, which must exist. The privileges
 * and rules of the change are checked only when it is made.
 */
@Component
public class PlanProposer {

    static final String TOOL = "propose_plan";

    /** Stands for an id an earlier change produces, while the schema is checked. */
    private static final String SOME_ID = "00000000-0000-0000-0000-000000000000";

    private final ToolRegistry registry;
    private final ToolCalls calls;
    private final ObjectMapper json;

    public PlanProposer(ToolRegistry registry, ToolCalls calls, ObjectMapper json) {
        this.registry = registry;
        this.calls = calls;
        this.json = json;
    }

    /** What the model is told once the plan is proposed. */
    static String proposed(String planId) {
        return "The plan " + planId + " waits for the editor: stop here; they approve it, or write to you instead.";
    }

    ModelTool tool() {
        return new ModelTool(
                TOOL,
                "Proposes a plan: every change to the archive, in order, each a write tool with its arguments. Read a"
                        + " tool with describe_write_tool before you use it. In place of an id an earlier change"
                        + " produces write {\"$ref\": N}, N its number counted from 1. The editor approves or drops"
                        + " the plan as a whole. The write tools: "
                        + String.join(
                                ", ",
                                registry.writeTools().stream()
                                        .map(t -> t.name())
                                        .toList())
                        + ".",
                """
                {
                  "type": "object",
                  "properties": {
                    "title": {"type": "string", "description": "What the plan does, in a line."},
                    "actions": {
                      "type": "array",
                      "minItems": 1,
                      "items": {
                        "type": "object",
                        "properties": {
                          "tool": {"type": "string"},
                          "arguments": {"type": "object"}
                        },
                        "required": ["tool", "arguments"],
                        "additionalProperties": false
                      }
                    }
                  },
                  "required": ["title", "actions"],
                  "additionalProperties": false
                }
                """);
    }

    /** The plan as it is to be stored, or why it does not hold. */
    Proposal check(String arguments, ToolContext context) {
        JsonNode plan = tree(arguments);
        if (!plan.isObject()
                || !plan.path("title").isTextual()
                || !plan.path("actions").isArray()) {
            throw new ToolArgumentsException(
                    "A plan is {\"title\": ..., \"actions\": [{\"tool\": ..., \"arguments\":" + " {...}}, ...]}.");
        }
        Set<String> fields = Set.of("title", "actions");
        plan.fieldNames().forEachRemaining(field -> {
            if (!fields.contains(field)) {
                throw new ToolArgumentsException("A plan has no field " + field + ".");
            }
        });
        if (plan.get("actions").isEmpty()) {
            throw new ToolArgumentsException("A plan holds at least one change.");
        }
        List<PlanAction> actions = new ArrayList<>();
        int position = 0;
        for (JsonNode action : plan.get("actions")) {
            position++;
            try {
                actions.add(action(action, position, context));
            } catch (RuntimeException e) {
                throw new ToolArgumentsException(
                        "Change " + position + " (" + action.path("tool").asText("?") + "): " + e.getMessage());
            }
        }
        return new Proposal(new PlanTitle(plan.get("title").asText()), actions);
    }

    private PlanAction action(JsonNode action, int position, ToolContext context) {
        Set<String> fields = Set.of("tool", "arguments");
        action.fieldNames().forEachRemaining(field -> {
            if (!fields.contains(field)) {
                throw new ToolArgumentsException("a change has only a tool and its arguments, not " + field + ".");
            }
        });
        JsonNode arguments = action.path("arguments");
        if (!action.path("tool").isTextual() || !arguments.isObject()) {
            throw new ToolArgumentsException("a change is {\"tool\": ..., \"arguments\": {...}}.");
        }
        String name = action.get("tool").asText();
        if (!(registry.tool(name) instanceof WriteTool<?> tool)) {
            throw new ToolArgumentsException(name + " only reads the archive: call it instead of planning it.");
        }
        List<Integer> dependsOn = PlanReferences.of(arguments, position);
        @Nullable String state = null;
        if (dependsOn.isEmpty()) {
            state = write(calls.state(name, arguments.toString(), context));
        } else {
            Map<Integer, String> stand = new HashMap<>();
            dependsOn.forEach(earlier -> stand.put(earlier, SOME_ID));
            calls.check(name, PlanReferences.resolve(arguments, stand).toString());
        }
        return PlanAction.proposed(
                name, arguments.toString(), dependsOn, tool.kind().code(), tool.entityType(), state);
    }

    private @Nullable String write(@Nullable Object state) {
        if (state == null) {
            return null;
        }
        try {
            return json.writeValueAsString(state);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("A state cannot be written as JSON", e);
        }
    }

    private JsonNode tree(String arguments) {
        try {
            return json.readTree(arguments);
        } catch (JsonProcessingException e) {
            throw new ToolArgumentsException("The plan is not JSON: " + e.getOriginalMessage());
        }
    }

    /** A plan that holds: its title and its changes. */
    record Proposal(PlanTitle title, List<PlanAction> actions) {}
}
