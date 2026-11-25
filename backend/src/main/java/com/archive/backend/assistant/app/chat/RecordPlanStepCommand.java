package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.PlanStep;

public record RecordPlanStepCommand(PlanId planId, PlanStep step) {}
