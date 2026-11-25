package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import com.fasterxml.jackson.databind.node.TextNode;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * {@code {"$ref": N}} in the arguments of a change of a plan, at any depth, in place of an id: the id the change N of
 * the same plan, counted from 1, produces. Only an earlier change can be referred to.
 */
public final class PlanReferences {

    static final String REF = "$ref";

    private PlanReferences() {}

    /**
     * The changes the arguments of the change at the position refer to, in their order; a reference to it or to a
     * later one, or one that is not a number, is refused with a {@code ToolArgumentsException}.
     */
    public static List<Integer> of(JsonNode arguments, int position) {
        Set<Integer> found = new LinkedHashSet<>();
        collect(arguments, position, found);
        return new ArrayList<>(found);
    }

    /** The arguments with each reference replaced by the id of the change it refers to. */
    public static JsonNode resolve(JsonNode arguments, Map<Integer, String> produced) {
        JsonNode copy = arguments.deepCopy();
        return replace(copy, produced);
    }

    private static void collect(JsonNode node, int position, Set<Integer> found) {
        if (node.isObject()) {
            if (node.has(REF)) {
                found.add(target(node, position));
                return;
            }
            node.forEach(child -> collect(child, position, found));
        } else if (node.isArray()) {
            node.forEach(child -> collect(child, position, found));
        }
    }

    private static int target(JsonNode reference, int position) {
        JsonNode number = reference.get(REF);
        if (reference.size() != 1 || !number.isIntegralNumber() || number.asInt() < 1) {
            throw new ToolArgumentsException("A reference is {\"" + REF + "\": N}, N the number of an earlier change"
                    + " counted from 1, and nothing besides: " + reference);
        }
        if (number.asInt() >= position) {
            throw new ToolArgumentsException("Change " + position + " refers to change " + number.asInt()
                    + ": a change can take an id only from an earlier one.");
        }
        return number.asInt();
    }

    private static JsonNode replace(JsonNode node, Map<Integer, String> produced) {
        if (node instanceof ObjectNode object) {
            if (object.has(REF)) {
                String id = produced.get(object.get(REF).asInt());
                if (id == null) {
                    throw new ToolArgumentsException("Change " + object.get(REF).asInt() + " produced no id to take.");
                }
                return TextNode.valueOf(id);
            }
            object.properties().forEach(entry -> entry.setValue(replace(entry.getValue(), produced)));
        } else if (node instanceof ArrayNode array) {
            for (int i = 0; i < array.size(); i++) {
                array.set(i, replace(array.get(i), produced));
            }
        }
        return node;
    }
}
