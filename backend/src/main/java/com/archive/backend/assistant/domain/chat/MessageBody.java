package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import org.jspecify.annotations.Nullable;

/** The text of a message, possibly empty, and what it carries besides as a JSON object. */
@ValueObject
public record MessageBody(String text, @Nullable String payload) {

    public MessageBody {
        Objects.requireNonNull(text, "text");
    }
}
