package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** The place of a message in its conversation, from 1 on; 0 is before the first. */
@ValueObject
public record MessageSeq(long value) {

    public static final MessageSeq START = new MessageSeq(0);

    public MessageSeq {
        if (value < 0) {
            throw new IllegalArgumentException("A message seq is not negative: " + value);
        }
    }
}
