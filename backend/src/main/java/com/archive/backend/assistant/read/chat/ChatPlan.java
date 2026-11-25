package com.archive.backend.assistant.read.chat;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record ChatPlan(UUID id, String title, String status, Instant proposedAt, List<ChatPlanAction> actions) {}
