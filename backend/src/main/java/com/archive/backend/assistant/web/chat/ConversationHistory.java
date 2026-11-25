package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.app.ListSkillsHandler;
import com.archive.backend.assistant.app.ListSkillsQuery;
import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelToolCall;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.MissingNode;
import java.util.ArrayList;
import java.util.List;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * What the model is told of a stored conversation: the system prompt with the skills it may load, then each message
 * as the model wrote or read it. Pictures a tool showed are not kept.
 */
@Component
public class ConversationHistory {

    static final String STOPPED = "The editor stopped the assistant here.";
    private static final ObjectMapper JSON = new ObjectMapper();

    private final ListSkillsHandler skills;

    public ConversationHistory(ListSkillsHandler skills) {
        this.skills = skills;
    }

    public List<ModelMessage> build(List<ChatMessage> messages) {
        List<ModelMessage> told = new ArrayList<>();
        told.add(ModelMessage.of(ModelRole.SYSTEM, SystemPrompt.of(skills.handle(new ListSkillsQuery()))));
        for (ChatMessage message : messages) {
            JsonNode payload = payload(message.payload());
            switch (message.kind()) {
                case "text" ->
                    told.add(
                            message.role().equals("user")
                                    ? ModelMessage.of(ModelRole.USER, message.text() + page(payload.path("page")))
                                    : ModelMessage.of(ModelRole.ASSISTANT, message.text()));
                case "toolCall" ->
                    call(
                            told,
                            payload.path("callId").asText(),
                            payload.path("tool").asText(),
                            payload.path("arguments").asText(),
                            payload.path("result").asText());
                case "plan" ->
                    call(
                            told,
                            payload.path("callId").asText(),
                            PlanProposer.TOOL,
                            payload.path("arguments").asText(),
                            PlanProposer.proposed(payload.path("planId").asText()));
                case "planProgress" -> told.add(ModelMessage.of(ModelRole.SYSTEM, message.text()));
                case "stopped" -> told.add(ModelMessage.of(ModelRole.SYSTEM, STOPPED));
                default -> {
                    // errors are the editor's to read: the model is not told of them
                }
            }
        }
        return told;
    }

    private static void call(List<ModelMessage> told, String callId, String tool, String arguments, String result) {
        told.add(ModelMessage.calls(List.of(new ModelToolCall(callId, tool, arguments))));
        told.add(ModelMessage.result(callId, result));
    }

    private static String page(JsonNode page) {
        String path = page.path("path").asText("");
        if (path.isEmpty()) {
            return "";
        }
        StringBuilder note = new StringBuilder("\n\n(The editor wrote this on the page ").append(path);
        String title = page.path("title").asText("");
        if (!title.isEmpty()) {
            note.append(" \"").append(title).append('"');
        }
        String type = page.path("entityType").asText("");
        String id = page.path("entityId").asText("");
        if (!type.isEmpty() && !id.isEmpty()) {
            note.append(", which shows [[").append(type).append(':').append(id).append("]]");
        }
        return note.append(".)").toString();
    }

    private static JsonNode payload(@Nullable String payload) {
        if (payload == null) {
            return MissingNode.getInstance();
        }
        try {
            return JSON.readTree(payload);
        } catch (JsonProcessingException e) {
            return MissingNode.getInstance();
        }
    }
}
