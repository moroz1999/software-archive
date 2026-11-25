package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunUsage;

public record RecordRunStepCommand(RunId runId, RunUsage step) {}
