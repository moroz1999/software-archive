package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.ConversationMessage;
import com.archive.backend.assistant.domain.chat.ConversationMessageRepository;
import com.archive.backend.assistant.domain.chat.MessageSeq;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** Each message takes the next seq of its conversation under the lock of the conversation's row. */
@Repository
@RequiredArgsConstructor
public class JdbcConversationMessageRepository implements ConversationMessageRepository {

    private final JdbcClient jdbc;

    @Override
    public MessageSeq save(ConversationMessage message) {
        long seq = jdbc.sql("""
                        UPDATE assistant_conversation SET last_seq = last_seq + 1, last_message_at = :at
                        WHERE id = :id RETURNING last_seq""")
                .param("id", message.getConversationId().value())
                .param("at", ChatRows.at(message.getCreatedAt()))
                .query(Long.class)
                .single();
        jdbc.sql("""
                        INSERT INTO assistant_message (conversation_id, seq, role, kind, text, payload, created_at)
                        VALUES (:id, :seq, :role, :kind, :text, CAST(:payload AS jsonb), :at)""")
                .param("id", message.getConversationId().value())
                .param("seq", seq)
                .param("role", message.getRole().name())
                .param("kind", message.getKind().name())
                .param("text", message.getBody().text())
                .param("payload", message.getBody().payload())
                .param("at", ChatRows.at(message.getCreatedAt()))
                .update();
        return new MessageSeq(seq);
    }
}
