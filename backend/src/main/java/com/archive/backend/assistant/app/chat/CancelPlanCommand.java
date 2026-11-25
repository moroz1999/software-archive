package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.user.domain.Actor;

public record CancelPlanCommand(Actor actor, PlanId planId) {}
