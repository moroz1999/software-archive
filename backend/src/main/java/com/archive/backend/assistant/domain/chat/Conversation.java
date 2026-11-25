package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.UserId;
import java.time.Instant;
import java.util.Objects;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

/**
 * A conversation of an editor with the assistant. Only its owner sees it: for anybody else it does not exist. It is
 * titled after its first message.
 */
@Getter
@AggregateRoot
public final class Conversation {

    private final ConversationId id;
    private final UserId owner;
    private final Instant createdAt;
    private @Nullable ConversationTitle title;
    private Instant lastMessageAt;

    /** A conversation as it is stored. */
    public Conversation(
            ConversationId id,
            UserId owner,
            Instant createdAt,
            @Nullable ConversationTitle title,
            Instant lastMessageAt) {
        this.id = Objects.requireNonNull(id, "id");
        this.owner = Objects.requireNonNull(owner, "owner");
        this.createdAt = Objects.requireNonNull(createdAt, "createdAt");
        this.title = title;
        this.lastMessageAt = Objects.requireNonNull(lastMessageAt, "lastMessageAt");
    }

    public static Conversation start(ConversationId id, UserId owner, Instant now) {
        return new Conversation(id, owner, now, null, now);
    }

    /** That it is the actor's; to anybody else it does not exist. */
    public void requireOwner(Actor actor) {
        if (!owner.equals(actor.id())) {
            throw new ConversationNotFoundException();
        }
    }

    /** An editor wrote to it: the first message titles it. */
    public void told(MessageText text, Instant now) {
        if (title == null) {
            title = ConversationTitle.of(text);
        }
        touched(now);
    }

    /** The assistant or a plan added to it. */
    public void touched(Instant now) {
        lastMessageAt = Objects.requireNonNull(now, "now");
    }
}
