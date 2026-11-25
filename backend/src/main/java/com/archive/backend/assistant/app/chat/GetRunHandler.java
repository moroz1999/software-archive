package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.read.chat.ChatReadRepository;
import com.archive.backend.assistant.read.chat.ChatRun;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A run as its runner sees it: whether it was asked to stop. */
@Service
@RequiredArgsConstructor
public class GetRunHandler {

    private final ChatReadRepository chats;

    @Transactional(readOnly = true)
    public ChatRun handle(GetRunQuery c) {
        return chats.run(c.runId().value())
                .orElseThrow(
                        () -> new IllegalStateException("No run " + c.runId().value()));
    }
}
