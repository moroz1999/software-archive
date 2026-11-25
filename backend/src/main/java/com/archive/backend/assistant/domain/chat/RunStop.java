package com.archive.backend.assistant.domain.chat;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** Whether the editor asked a run to stop. */
@ValueObject
public enum RunStop {
    NOT_ASKED,
    ASKED
}
