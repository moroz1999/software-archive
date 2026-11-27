package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.PlanAction;
import com.archive.backend.assistant.domain.chat.PlanActionStatus;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** How the chat is kept in rows: enums by their names, the changes of a plan as one JSON array. */
final class ChatRows {

    private ChatRows() {}

    /** A change of a plan as it is kept. */
    record ActionRow(
            String tool,
            String arguments,
            List<Integer> dependsOn,
            String kind,
            String entityType,
            @Nullable String state,
            String status,
            @Nullable String result,
            @Nullable String error) {

        static ActionRow of(PlanAction action) {
            return new ActionRow(
                    action.tool(),
                    action.arguments(),
                    action.dependsOn(),
                    action.kind(),
                    action.entityType(),
                    action.state(),
                    action.status().name(),
                    action.result(),
                    action.error());
        }

        PlanAction toAction() {
            return new PlanAction(
                    tool,
                    arguments,
                    dependsOn,
                    kind,
                    entityType,
                    state,
                    PlanActionStatus.valueOf(status),
                    result,
                    error);
        }
    }

    static String actions(ObjectMapper json, List<PlanAction> actions) {
        try {
            return json.writeValueAsString(actions.stream().map(ActionRow::of).toList());
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("The changes of a plan cannot be written as JSON", e);
        }
    }

    static List<ActionRow> actions(ObjectMapper json, String text) {
        try {
            return json.readValue(text, new TypeReference<List<ActionRow>>() {});
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("The changes of a plan cannot be read: " + text, e);
        }
    }

    static Timestamp at(Instant instant) {
        return Timestamp.from(instant);
    }

    static @Nullable Timestamp at(@Nullable Instant instant, boolean nullable) {
        return instant == null ? null : Timestamp.from(instant);
    }

    static Instant instant(ResultSet rs, String column) throws SQLException {
        return rs.getTimestamp(column).toInstant();
    }

    static @Nullable Instant nullableInstant(ResultSet rs, String column) throws SQLException {
        Timestamp at = rs.getTimestamp(column);
        return at == null ? null : at.toInstant();
    }
}
