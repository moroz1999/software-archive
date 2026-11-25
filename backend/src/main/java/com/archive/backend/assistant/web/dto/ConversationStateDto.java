package com.archive.backend.assistant.web.dto;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

public record ConversationStateDto(
        UUID id,
        @Nullable String title,
        List<ChatMessageDto> messages,
        List<ChatPlanDto> plans,
        @Nullable ChatRunDto activeRun,
        Map<String, EntityRefDto> refs) {}
