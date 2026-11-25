package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;

/** What an editor writes to the assistant: not blank, at most 20 000 characters. */
@ValueObject
public record MessageText(String value) {

    public static final int MAX_LENGTH = 20_000;

    public MessageText {
        value = value.strip();
        if (value.isEmpty()) {
            throw new InvalidValueException("assistant.message-empty", "Write what the assistant should do.");
        }
        if (value.length() > MAX_LENGTH) {
            throw new InvalidValueException(
                    "assistant.message-too-long", "A message is at most " + MAX_LENGTH + " characters long.");
        }
    }
}
