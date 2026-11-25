package com.archive.backend.assistant.app.chat;

import com.archive.backend.assistant.domain.chat.ConversationNotFoundException;
import com.archive.backend.assistant.read.chat.ChatMessage;
import com.archive.backend.assistant.read.chat.ChatPlan;
import com.archive.backend.assistant.read.chat.ChatPlanAction;
import com.archive.backend.assistant.read.chat.ChatReadRepository;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import com.archive.backend.assistant.read.chat.ConversationState;
import com.archive.backend.assistant.read.chat.EntityRef;
import com.archive.backend.assistant.read.chat.EntityRefReadRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A conversation of the editor: the messages after the seq, the plans, the run working on it and the entries they mention. */
@Service
@RequiredArgsConstructor
public class GetConversationStateHandler {

    private final ChatReadRepository chats;
    private final EntityRefReadRepository refs;
    private final ObjectMapper json;

    @Transactional(readOnly = true)
    public ConversationState handle(GetConversationStateQuery c) {
        ChatAccess.requireUser(c.actor());
        UUID id = c.conversationId().value();
        if (!chats.owner(id)
                .map(owner -> owner.equals(c.actor().signedIn().value()))
                .orElse(false)) {
            throw new ConversationNotFoundException();
        }
        List<ChatMessage> messages = chats.messages(id, c.after().value());
        List<ChatPlan> plans = chats.plans(id);
        Mentions mentions = new Mentions(json);
        messages.forEach(message -> mentions.inText(message.text()));
        for (ChatPlan plan : plans) {
            for (ChatPlanAction action : plan.actions()) {
                mentions.inJson(action.arguments());
                mentions.inJson(action.state());
                mentions.id(action.result());
            }
        }
        Map<String, EntityRef> found = new LinkedHashMap<>();
        refs.byIds(mentions.ids()).forEach(ref -> found.put(ref.id(), ref));
        refs.hardware(mentions.hardware()).forEach(ref -> found.put(ref.id(), ref));
        return new ConversationState(
                id,
                chats.conversations(c.actor().signedIn().value()).stream()
                        .filter(entry -> entry.id().equals(id))
                        .map(ConversationListEntry::title)
                        .findFirst()
                        .orElse(null),
                messages,
                plans,
                chats.activeRun(id).orElse(null),
                found);
    }
}
