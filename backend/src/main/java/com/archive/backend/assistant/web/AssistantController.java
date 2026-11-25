package com.archive.backend.assistant.web;

import com.archive.backend.assistant.web.chat.ChatService;
import com.archive.backend.assistant.web.dto.ConversationCreatedDto;
import com.archive.backend.assistant.web.dto.ConversationListEntryDto;
import com.archive.backend.assistant.web.dto.ConversationStateDto;
import com.archive.backend.assistant.web.dto.SendMessageRequestDto;
import com.archive.backend.assistant.web.mapper.AssistantWebMapper;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The chat with the AI assistant, for the members of its group (docs/features/ai-assistant.md#chat). A message and an
 * approval answer at once: the panel polls the conversation while its run works.
 */
@RestController
@RequestMapping("/api/assistant")
@RequiredArgsConstructor
public class AssistantController {

    private final ChatService chat;
    private final AssistantWebMapper mapper;

    @GetMapping("/conversations")
    public List<ConversationListEntryDto> conversations(Actor actor) {
        return mapper.toList(chat.conversations(actor));
    }

    @PostMapping("/conversations")
    @ResponseStatus(HttpStatus.CREATED)
    public ConversationCreatedDto create(Actor actor) {
        return new ConversationCreatedDto(chat.create(actor));
    }

    @GetMapping("/conversations/{id}")
    public ConversationStateDto state(
            Actor actor, @PathVariable UUID id, @RequestParam(defaultValue = "0") long afterSeq) {
        return mapper.toDto(chat.state(actor, id, afterSeq));
    }

    @PostMapping("/conversations/{id}/messages")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void send(
            Actor actor, Language language, @PathVariable UUID id, @RequestBody SendMessageRequestDto request) {
        chat.send(actor, language, id, request.text(), mapper.toPage(request.page()));
    }

    @PostMapping("/conversations/{id}/stop")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void stop(Actor actor, @PathVariable UUID id) {
        chat.stop(actor, id);
    }

    @DeleteMapping("/conversations/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Actor actor, @PathVariable UUID id) {
        chat.delete(actor, id);
    }

    @PostMapping("/plans/{id}/approve")
    @ResponseStatus(HttpStatus.ACCEPTED)
    public void approve(Actor actor, Language language, @PathVariable UUID id) {
        chat.approve(actor, language, id);
    }

    @PostMapping("/plans/{id}/cancel")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void cancel(Actor actor, @PathVariable UUID id) {
        chat.cancel(actor, id);
    }
}
