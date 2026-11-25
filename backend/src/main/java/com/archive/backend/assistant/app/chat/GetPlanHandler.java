package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.read.chat.ChatPlan;
import com.archive.backend.assistant.read.chat.ChatReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A plan, for the run making it. */
@Service
@RequiredArgsConstructor
public class GetPlanHandler {

    private final ChatReadRepository chats;

    @Transactional(readOnly = true)
    public ChatPlan handle(GetPlanQuery c) {
        return chats.plan(c.planId().value())
                .orElseThrow(
                        () -> new IllegalStateException("No plan " + c.planId().value()));
    }
}
