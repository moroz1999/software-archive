package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.app.chat.ApprovePlanCommand;
import com.archive.backend.assistant.app.chat.ApprovePlanHandler;
import com.archive.backend.assistant.app.chat.CancelPlanCommand;
import com.archive.backend.assistant.app.chat.CancelPlanHandler;
import com.archive.backend.assistant.app.chat.CreateConversationCommand;
import com.archive.backend.assistant.app.chat.CreateConversationHandler;
import com.archive.backend.assistant.app.chat.DeleteConversationCommand;
import com.archive.backend.assistant.app.chat.DeleteConversationHandler;
import com.archive.backend.assistant.app.chat.GetConversationStateHandler;
import com.archive.backend.assistant.app.chat.GetConversationStateQuery;
import com.archive.backend.assistant.app.chat.GetRunHandler;
import com.archive.backend.assistant.app.chat.GetRunQuery;
import com.archive.backend.assistant.app.chat.ListConversationsHandler;
import com.archive.backend.assistant.app.chat.ListConversationsQuery;
import com.archive.backend.assistant.app.chat.SendMessageCommand;
import com.archive.backend.assistant.app.chat.SendMessageHandler;
import com.archive.backend.assistant.app.chat.StopRunCommand;
import com.archive.backend.assistant.app.chat.StopRunHandler;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageSeq;
import com.archive.backend.assistant.domain.chat.MessageText;
import com.archive.backend.assistant.domain.chat.PageContext;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.read.chat.ConversationListEntry;
import com.archive.backend.assistant.read.chat.ConversationState;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

/**
 * The chat with the assistant, whatever carries it: the REST controller now, a socket or an MCP server later. Sending
 * a message and approving a plan answer at once; the run they start goes on in the background, as the editor.
 */
@Component
@RequiredArgsConstructor
public class ChatService {

    private final RunLauncher launcher;
    private final ListConversationsHandler list;
    private final CreateConversationHandler create;
    private final GetConversationStateHandler state;
    private final SendMessageHandler send;
    private final StopRunHandler stop;
    private final ApprovePlanHandler approve;
    private final CancelPlanHandler cancel;
    private final DeleteConversationHandler delete;
    private final GetRunHandler runs;
    private final AgentRunner agent;
    private final PlanExecutor executor;

    public List<ConversationListEntry> conversations(Actor actor) {
        return list.handle(new ListConversationsQuery(actor));
    }

    public UUID create(Actor actor) {
        return create.handle(new CreateConversationCommand(actor)).value();
    }

    public ConversationState state(Actor actor, UUID conversationId, long afterSeq) {
        return state.handle(
                new GetConversationStateQuery(actor, new ConversationId(conversationId), new MessageSeq(afterSeq)));
    }

    public void send(Actor actor, Language language, UUID conversationId, String text, PageContext page) {
        ConversationId conversation = new ConversationId(conversationId);
        RunId run = send.handle(new SendMessageCommand(actor, conversation, new MessageText(text), page));
        ToolContext context = new ToolContext(actor, language);
        launcher.launch(() -> agent.run(run, conversation, context));
    }

    public void stop(Actor actor, UUID conversationId) {
        stop.handle(new StopRunCommand(actor, new ConversationId(conversationId)));
    }

    public void approve(Actor actor, Language language, UUID planId) {
        PlanId plan = new PlanId(planId);
        RunId run = approve.handle(new ApprovePlanCommand(actor, plan));
        ConversationId conversation =
                new ConversationId(runs.handle(new GetRunQuery(run)).conversationId());
        ToolContext context = new ToolContext(actor, language);
        launcher.launch(() -> executor.run(run, conversation, plan, context));
    }

    public void cancel(Actor actor, UUID planId) {
        cancel.handle(new CancelPlanCommand(actor, new PlanId(planId)));
    }

    public void delete(Actor actor, UUID conversationId) {
        delete.handle(new DeleteConversationCommand(actor, new ConversationId(conversationId)));
    }
}
