package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

/** The title of a conversation: its first message, cut to 80 characters. */
@ValueObject
public record ConversationTitle(String value) {

    public static final int MAX_LENGTH = 80;

    public ConversationTitle {
        Objects.requireNonNull(value, "value");
    }

    public static ConversationTitle of(MessageText first) {
        String line = first.value().replaceAll("\\s+", " ");
        return new ConversationTitle(
                line.codePointCount(0, line.length()) <= MAX_LENGTH
                        ? line
                        : line.substring(0, line.offsetByCodePoints(0, MAX_LENGTH))
                                .strip());
    }
}
