package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.app.chat.ClaimRunCommand;
import com.archive.backend.assistant.app.chat.ClaimRunHandler;
import com.archive.backend.assistant.app.chat.EndRunCommand;
import com.archive.backend.assistant.app.chat.EndRunHandler;
import com.archive.backend.assistant.app.chat.GetRunHandler;
import com.archive.backend.assistant.app.chat.GetRunQuery;
import com.archive.backend.assistant.app.chat.RecordMessageCommand;
import com.archive.backend.assistant.app.chat.RecordMessageHandler;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageBody;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.RunError;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/** What both kinds of run do with their run and their conversation: each step stored before it is acted on. */
@Component
@RequiredArgsConstructor
class ChatSteps {

    private final ClaimRunHandler claim;
    private final GetRunHandler runs;
    private final EndRunHandler end;
    private final RecordMessageHandler record;
    private final ObjectMapper json;

    boolean claim(RunId run) {
        return claim.handle(new ClaimRunCommand(run));
    }

    boolean stopRequested(RunId run) {
        return runs.handle(new GetRunQuery(run)).stopRequested();
    }

    void message(
            ConversationId conversation,
            MessageRole role,
            MessageKind kind,
            String text,
            @Nullable Map<String, ?> payload) {
        record.handle(new RecordMessageCommand(conversation, role, kind, new MessageBody(text, json(payload))));
    }

    void end(RunId run, RunStatus status) {
        end.handle(new EndRunCommand(run, status, null));
    }

    /** Ends the run failed, telling the editor why. */
    void fail(RunId run, ConversationId conversation, String why) {
        message(conversation, MessageRole.SYSTEM, MessageKind.ERROR, why, null);
        end.handle(new EndRunCommand(run, RunStatus.FAILED, new RunError(why)));
    }

    /** Ends the run stopped, telling the model and the editor where. */
    void stopped(RunId run, ConversationId conversation) {
        message(conversation, MessageRole.SYSTEM, MessageKind.STOPPED, "", null);
        end(run, RunStatus.STOPPED);
    }

    String json(@Nullable Object value) {
        if (value == null) {
            return "null";
        }
        try {
            return json.writeValueAsString(value);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("Cannot be written as JSON: " + value.getClass(), e);
        }
    }

    private @Nullable String json(@Nullable Map<String, ?> payload) {
        return payload == null ? null : json((Object) payload);
    }
}
