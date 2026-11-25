package com.archive.backend.assistant.domain.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import java.time.Instant;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ConversationTest {

    private static final Instant NOW = Instant.parse("2026-11-25T10:00:00Z");
    private static final UserId OWNER = new UserId(UUID.randomUUID());

    @Test
    void onlyItsOwnerSeesIt() {
        Conversation conversation = Conversation.start(new ConversationId(UUID.randomUUID()), OWNER, NOW);

        conversation.requireOwner(new Actor(OWNER, Set.of(UserGroup.MEMBER, UserGroup.AI_ASSISTANT)));
        assertThatThrownBy(() -> conversation.requireOwner(
                        new Actor(new UserId(UUID.randomUUID()), Set.of(UserGroup.MEMBER, UserGroup.AI_ASSISTANT))))
                .isInstanceOf(ConversationNotFoundException.class);
        assertThatThrownBy(() -> conversation.requireOwner(Actor.anonymous()))
                .isInstanceOf(ConversationNotFoundException.class);
    }

    @Test
    void isTitledAfterItsFirstMessage() {
        Conversation conversation = Conversation.start(new ConversationId(UUID.randomUUID()), OWNER, NOW);
        assertThat(conversation.getTitle()).isNull();

        conversation.told(new MessageText("Добавь Elite"), NOW.plusSeconds(10));
        conversation.told(new MessageText("И релиз тоже"), NOW.plusSeconds(20));

        assertThat(conversation.getTitle()).isEqualTo(new ConversationTitle("Добавь Elite"));
        assertThat(conversation.getLastMessageAt()).isEqualTo(NOW.plusSeconds(20));
    }

    @Test
    void aTitleIsTheFirstMessageOnOneLineCutTo80Characters() {
        assertThat(ConversationTitle.of(new MessageText("Добавь\nигру   Elite")).value())
                .isEqualTo("Добавь игру Elite");
        assertThat(ConversationTitle.of(new MessageText("я".repeat(100))).value())
                .isEqualTo("я".repeat(80));
    }

    @Test
    void aMessageIsNotBlankAndAtMost20000Characters() {
        assertThat(new MessageText("  Привет  ").value()).isEqualTo("Привет");
        assertThatThrownBy(() -> new MessageText("   "))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("code", "assistant.message-empty");
        assertThat(new MessageText("я".repeat(20_000)).value()).hasSize(20_000);
        assertThatThrownBy(() -> new MessageText("я".repeat(20_001)))
                .isInstanceOf(InvalidValueException.class)
                .hasFieldOrPropertyWithValue("code", "assistant.message-too-long");
    }
}
