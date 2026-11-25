package com.archive.backend.assistant.app.chat;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * The entries a conversation mentions: {@code [[type:id]]} in prose, and in the changes of plans the ids of fields
 * named {@code id}, {@code <name>Id} or {@code <name>Ids}, and what a change produced. A name tells no type, so the
 * ids are found among all entries.
 */
final class Mentions {

    private static final Pattern MENTION = Pattern.compile("\\[\\[(\\w+):([^\\]\\s]+)]]");

    private final ObjectMapper json;
    private final Set<UUID> ids = new LinkedHashSet<>();
    private final Set<String> hardware = new LinkedHashSet<>();

    Mentions(ObjectMapper json) {
        this.json = json;
    }

    void inText(String text) {
        Matcher mention = MENTION.matcher(text);
        while (mention.find()) {
            if (mention.group(1).equals("hardware")) {
                hardware.add(mention.group(2));
            } else {
                id(mention.group(2));
            }
        }
    }

    void inJson(@Nullable String text) {
        if (text == null) {
            return;
        }
        try {
            walk(json.readTree(text), null);
        } catch (JsonProcessingException e) {
            // arguments the model wrote badly name nothing
        }
    }

    void id(@Nullable String text) {
        if (text == null) {
            return;
        }
        try {
            ids.add(UUID.fromString(text));
        } catch (IllegalArgumentException e) {
            // not an id of an entry: the code of hardware, or nothing
        }
    }

    Set<UUID> ids() {
        return Set.copyOf(ids);
    }

    Set<String> hardware() {
        return Set.copyOf(hardware);
    }

    private void walk(JsonNode node, @Nullable String field) {
        if (node.isObject()) {
            for (Map.Entry<String, JsonNode> entry : node.properties()) {
                walk(entry.getValue(), entry.getKey());
            }
        } else if (node.isArray()) {
            node.forEach(item -> walk(item, field));
        } else if (node.isTextual() && field != null && names(field)) {
            id(node.asText());
        }
    }

    private static boolean names(String field) {
        return field.equals("id") || field.endsWith("Id") || field.endsWith("Ids");
    }
}
