package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.Conversation;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.ConversationRepository;
import com.archive.backend.assistant.domain.chat.ConversationTitle;
import com.archive.backend.user.domain.UserId;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcConversationRepository implements ConversationRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<Conversation> findById(ConversationId id) {
        return jdbc.sql("""
                        SELECT owner_id, title, created_at, last_message_at FROM assistant_conversation
                        WHERE id = :id FOR UPDATE""")
                .param("id", id.value())
                .query((rs, n) -> {
                    String title = rs.getString("title");
                    return new Conversation(
                            id,
                            new UserId(rs.getObject("owner_id", UUID.class)),
                            ChatRows.instant(rs, "created_at"),
                            title == null ? null : new ConversationTitle(title),
                            ChatRows.instant(rs, "last_message_at"));
                })
                .optional();
    }

    @Override
    public void save(Conversation conversation) {
        ConversationTitle title = conversation.getTitle();
        jdbc.sql("""
                        INSERT INTO assistant_conversation (id, owner_id, title, created_at, last_message_at)
                        VALUES (:id, :owner, :title, :created, :last)
                        ON CONFLICT (id) DO UPDATE SET title = EXCLUDED.title, last_message_at = EXCLUDED.last_message_at""")
                .param("id", conversation.getId().value())
                .param("owner", conversation.getOwner().value())
                .param("title", title == null ? null : title.value())
                .param("created", ChatRows.at(conversation.getCreatedAt()))
                .param("last", ChatRows.at(conversation.getLastMessageAt()))
                .update();
    }

    @Override
    public void delete(Conversation conversation) {
        jdbc.sql("DELETE FROM assistant_conversation WHERE id = :id")
                .param("id", conversation.getId().value())
                .update();
    }
}
