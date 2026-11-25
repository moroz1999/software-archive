package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.Run;
import com.archive.backend.assistant.domain.chat.RunError;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunRepository;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.assistant.domain.chat.RunStop;
import com.archive.backend.assistant.domain.chat.RunType;
import com.archive.backend.assistant.domain.chat.RunUsage;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** A run read to be changed is locked until the change commits: two cannot claim it. */
@Repository
@RequiredArgsConstructor
public class JdbcRunRepository implements RunRepository {

    private static final String COLUMNS = "SELECT id, conversation_id, type, plan_id, status, stop, queued_at, steps,"
            + " tokens_in, tokens_out, error, finished_at FROM assistant_run";
    private static final String ACTIVE = "status IN ('QUEUED', 'RUNNING')";

    private final JdbcClient jdbc;

    @Override
    public Optional<Run> findById(RunId id) {
        return jdbc.sql(COLUMNS + " WHERE id = :id FOR UPDATE")
                .param("id", id.value())
                .query((rs, n) -> run(rs))
                .optional();
    }

    @Override
    public Optional<Run> findActive(ConversationId conversationId) {
        return jdbc.sql(COLUMNS + " WHERE conversation_id = :id AND " + ACTIVE
                        + " ORDER BY queued_at LIMIT 1 FOR UPDATE")
                .param("id", conversationId.value())
                .query((rs, n) -> run(rs))
                .optional();
    }

    @Override
    public List<Run> findActive() {
        return jdbc.sql(COLUMNS + " WHERE " + ACTIVE + " FOR UPDATE")
                .query((rs, n) -> run(rs))
                .list();
    }

    @Override
    public void save(Run run) {
        PlanId plan = run.getPlanId();
        RunError error = run.getError();
        jdbc.sql("""
                        INSERT INTO assistant_run (id, conversation_id, type, plan_id, status, stop, queued_at, steps,
                            tokens_in, tokens_out, error, finished_at)
                        VALUES (:id, :conversation, :type, :plan, :status, :stop, :queued, :steps, :in, :out, :error,
                            :finished)
                        ON CONFLICT (id) DO UPDATE SET status = EXCLUDED.status, stop = EXCLUDED.stop,
                            steps = EXCLUDED.steps, tokens_in = EXCLUDED.tokens_in, tokens_out = EXCLUDED.tokens_out,
                            error = EXCLUDED.error, finished_at = EXCLUDED.finished_at""")
                .param("id", run.getId().value())
                .param("conversation", run.getConversationId().value())
                .param("type", run.getType().name())
                .param("plan", plan == null ? null : plan.value())
                .param("status", run.getStatus().name())
                .param("stop", run.getStop().name())
                .param("queued", ChatRows.at(run.getQueuedAt()))
                .param("steps", run.getUsage().steps())
                .param("in", run.getUsage().tokensIn())
                .param("out", run.getUsage().tokensOut())
                .param("error", error == null ? null : error.value())
                .param("finished", ChatRows.at(run.getFinishedAt(), true))
                .update();
    }

    private static Run run(ResultSet rs) throws SQLException {
        UUID plan = rs.getObject("plan_id", UUID.class);
        String error = rs.getString("error");
        return new Run(
                new RunId(rs.getObject("id", UUID.class)),
                new ConversationId(rs.getObject("conversation_id", UUID.class)),
                RunType.valueOf(rs.getString("type")),
                plan == null ? null : new PlanId(plan),
                ChatRows.instant(rs, "queued_at"),
                RunStatus.valueOf(rs.getString("status")),
                RunStop.valueOf(rs.getString("stop")),
                new RunUsage(rs.getInt("steps"), rs.getLong("tokens_in"), rs.getLong("tokens_out")),
                error == null ? null : new RunError(error),
                ChatRows.nullableInstant(rs, "finished_at"));
    }
}
