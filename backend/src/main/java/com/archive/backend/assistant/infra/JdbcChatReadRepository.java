package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.PlanActionStatus;
import com.archive.backend.assistant.domain.chat.PlanStatus;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.assistant.domain.chat.RunType;
import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.chat.ChatPlan;
import com.archive.backend.assistant.read.chat.ChatPlanAction;
import com.archive.backend.assistant.read.chat.ChatReadRepository;
import com.archive.backend.assistant.read.chat.ChatRun;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcChatReadRepository implements ChatReadRepository {

    private static final String RUN =
            "SELECT id, conversation_id, type, plan_id, status, stop, queued_at, steps, error" + " FROM assistant_run";
    private static final String PLAN = "SELECT id, title, status, proposed_at, actions FROM assistant_plan";

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    @Override
    public List<ConversationListEntry> conversations(UUID owner) {
        return jdbc.sql("""
                        SELECT c.id, c.title, c.last_message_at,
                            EXISTS (SELECT 1 FROM assistant_run r WHERE r.conversation_id = c.id
                                AND r.status IN ('QUEUED', 'RUNNING')) AS running
                        FROM assistant_conversation c WHERE c.owner_id = :owner
                        ORDER BY c.last_message_at DESC, c.created_at DESC""")
                .param("owner", owner)
                .query((rs, n) -> new ConversationListEntry(
                        rs.getObject("id", UUID.class),
                        rs.getString("title"),
                        ChatRows.instant(rs, "last_message_at"),
                        rs.getBoolean("running")))
                .list();
    }

    @Override
    public Optional<UUID> owner(UUID conversationId) {
        return jdbc.sql("SELECT owner_id FROM assistant_conversation WHERE id = :id")
                .param("id", conversationId)
                .query(UUID.class)
                .optional();
    }

    @Override
    public List<ChatMessage> messages(UUID conversationId, long afterSeq) {
        return jdbc.sql("""
                        SELECT seq, role, kind, text, payload::text AS payload, created_at FROM assistant_message
                        WHERE conversation_id = :id AND seq > :after ORDER BY seq""")
                .param("id", conversationId)
                .param("after", afterSeq)
                .query((rs, n) -> new ChatMessage(
                        rs.getLong("seq"),
                        MessageRole.valueOf(rs.getString("role")).code(),
                        MessageKind.valueOf(rs.getString("kind")).code(),
                        rs.getString("text"),
                        rs.getString("payload"),
                        ChatRows.instant(rs, "created_at")))
                .list();
    }

    @Override
    public List<ChatPlan> plans(UUID conversationId) {
        return jdbc.sql(PLAN + " WHERE conversation_id = :id ORDER BY proposed_at, id")
                .param("id", conversationId)
                .query((rs, n) -> plan(rs))
                .list();
    }

    @Override
    public Optional<ChatPlan> plan(UUID planId) {
        return jdbc.sql(PLAN + " WHERE id = :id")
                .param("id", planId)
                .query((rs, n) -> plan(rs))
                .optional();
    }

    @Override
    public Optional<ChatRun> activeRun(UUID conversationId) {
        return jdbc.sql(RUN
                        + " WHERE conversation_id = :id AND status IN ('QUEUED', 'RUNNING') ORDER BY queued_at LIMIT 1")
                .param("id", conversationId)
                .query((rs, n) -> run(rs))
                .optional();
    }

    @Override
    public Optional<ChatRun> run(UUID runId) {
        return jdbc.sql(RUN + " WHERE id = :id")
                .param("id", runId)
                .query((rs, n) -> run(rs))
                .optional();
    }

    private ChatPlan plan(ResultSet rs) throws SQLException {
        List<ChatRows.ActionRow> rows = ChatRows.actions(json, rs.getString("actions"));
        List<ChatPlanAction> actions = new ArrayList<>();
        for (int i = 0; i < rows.size(); i++) {
            ChatRows.ActionRow row = rows.get(i);
            actions.add(new ChatPlanAction(
                    i + 1,
                    row.tool(),
                    row.arguments(),
                    row.dependsOn(),
                    row.kind(),
                    row.entityType(),
                    row.state(),
                    PlanActionStatus.valueOf(row.status()).code(),
                    row.result(),
                    row.error()));
        }
        return new ChatPlan(
                rs.getObject("id", UUID.class),
                rs.getString("title"),
                PlanStatus.valueOf(rs.getString("status")).code(),
                ChatRows.instant(rs, "proposed_at"),
                actions);
    }

    private static ChatRun run(ResultSet rs) throws SQLException {
        return new ChatRun(
                rs.getObject("id", UUID.class),
                rs.getObject("conversation_id", UUID.class),
                RunType.valueOf(rs.getString("type")).code(),
                rs.getObject("plan_id", UUID.class),
                RunStatus.valueOf(rs.getString("status")).code(),
                rs.getString("stop").equals("ASKED"),
                ChatRows.instant(rs, "queued_at"),
                rs.getInt("steps"),
                rs.getString("error"));
    }
}
