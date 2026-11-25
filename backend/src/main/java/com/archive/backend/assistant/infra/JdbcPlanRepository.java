package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.Plan;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.PlanRepository;
import com.archive.backend.assistant.domain.chat.PlanStatus;
import com.archive.backend.assistant.domain.chat.PlanTitle;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcPlanRepository implements PlanRepository {

    private static final String COLUMNS =
            "SELECT id, conversation_id, title, status, proposed_at, finished_at, actions FROM assistant_plan";

    private final JdbcClient jdbc;
    private final ObjectMapper json;

    @Override
    public Optional<Plan> findById(PlanId id) {
        return jdbc.sql(COLUMNS + " WHERE id = :id FOR UPDATE")
                .param("id", id.value())
                .query((rs, n) -> plan(rs))
                .optional();
    }

    @Override
    public Optional<Plan> findProposed(ConversationId conversationId) {
        return jdbc.sql(COLUMNS + " WHERE conversation_id = :id AND status = 'PROPOSED' FOR UPDATE")
                .param("id", conversationId.value())
                .query((rs, n) -> plan(rs))
                .optional();
    }

    @Override
    public List<Plan> findExecuting() {
        return jdbc.sql(COLUMNS + " WHERE status = 'EXECUTING' FOR UPDATE")
                .query((rs, n) -> plan(rs))
                .list();
    }

    @Override
    public void save(Plan plan) {
        jdbc.sql("""
                        INSERT INTO assistant_plan (id, conversation_id, title, status, proposed_at, finished_at, actions)
                        VALUES (:id, :conversation, :title, :status, :proposed, :finished, CAST(:actions AS jsonb))
                        ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, finished_at = EXCLUDED.finished_at,
                            actions = EXCLUDED.actions""")
                .param("id", plan.getId().value())
                .param("conversation", plan.getConversationId().value())
                .param("title", plan.getTitle().value())
                .param("status", plan.getStatus().name())
                .param("proposed", ChatRows.at(plan.getProposedAt()))
                .param("finished", ChatRows.at(plan.getFinishedAt(), true))
                .param("actions", ChatRows.actions(json, plan.getActions()))
                .update();
    }

    private Plan plan(ResultSet rs) throws SQLException {
        return new Plan(
                new PlanId(rs.getObject("id", UUID.class)),
                new ConversationId(rs.getObject("conversation_id", UUID.class)),
                new PlanTitle(rs.getString("title")),
                ChatRows.instant(rs, "proposed_at"),
                PlanStatus.valueOf(rs.getString("status")),
                ChatRows.actions(json, rs.getString("actions")).stream()
                        .map(ChatRows.ActionRow::toAction)
                        .toList(),
                ChatRows.nullableInstant(rs, "finished_at"));
    }
}
