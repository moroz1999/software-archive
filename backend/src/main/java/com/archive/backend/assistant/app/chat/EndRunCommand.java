package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.RunError;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunStatus;
import org.jspecify.annotations.Nullable;

public record EndRunCommand(
        RunId runId, RunStatus status, @Nullable RunError error) {}
