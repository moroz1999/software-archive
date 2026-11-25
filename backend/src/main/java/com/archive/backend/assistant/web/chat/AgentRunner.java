package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.app.chat.AskModelHandler;
import com.archive.backend.assistant.app.chat.AskModelQuery;
import com.archive.backend.assistant.app.chat.GetHistoryHandler;
import com.archive.backend.assistant.app.chat.GetHistoryQuery;
import com.archive.backend.assistant.app.chat.ProposePlanCommand;
import com.archive.backend.assistant.app.chat.ProposePlanHandler;
import com.archive.backend.assistant.app.chat.RecordRunStepCommand;
import com.archive.backend.assistant.app.chat.RecordRunStepHandler;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.assistant.domain.chat.RunUsage;
import com.archive.backend.assistant.read.model.ModelImage;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelReply;
import com.archive.backend.assistant.read.model.ModelRequest;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelTool;
import com.archive.backend.assistant.read.model.ModelToolCall;
import com.archive.backend.assistant.web.tool.ReadTool;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.ToolRegistry;
import com.archive.backend.remote.read.ViewedImage;
import com.archive.backend.shared.domain.error.BusinessException;
import java.time.Clock;
import java.time.Duration;
import java.time.Instant;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Answers a conversation: tells the model the conversation and the tools that read, runs each call it makes as the
 * editor and tells it the result, until it answers without a call, proposes a plan, is stopped, or runs out of its 50
 * steps or 5 minutes. A call that fails is told to the model, which goes on. A picture a call showed is shown to the
 * model in this run only.
 */
@Component
@RequiredArgsConstructor
public class AgentRunner {

    static final int MAX_STEPS = 50;
    static final Duration MAX_TIME = Duration.ofMinutes(5);
    private static final Logger log = LoggerFactory.getLogger(AgentRunner.class);

    private final ChatSteps steps;
    private final GetHistoryHandler history;
    private final ConversationHistory told;
    private final AskModelHandler model;
    private final RecordRunStepHandler usage;
    private final ProposePlanHandler propose;
    private final PlanProposer plans;
    private final ToolRegistry registry;
    private final ToolCalls calls;
    private final Clock clock;

    public void run(RunId run, ConversationId conversation, ToolContext context) {
        if (!steps.claim(run)) {
            return;
        }
        try {
            answer(run, conversation, context);
        } catch (BusinessException e) {
            steps.fail(run, conversation, String.valueOf(e.getMessage()));
        } catch (RuntimeException e) {
            log.error("A run of the assistant failed", e);
            steps.fail(run, conversation, "The assistant failed: " + e.getMessage());
        }
    }

    private void answer(RunId run, ConversationId conversation, ToolContext context) {
        Instant deadline = clock.instant().plus(MAX_TIME);
        Map<String, ModelMessage> pictures = new HashMap<>();
        for (int step = 0; ; step++) {
            if (steps.stopRequested(run)) {
                steps.stopped(run, conversation);
                return;
            }
            if (step == MAX_STEPS || clock.instant().isAfter(deadline)) {
                steps.fail(
                        run,
                        conversation,
                        "The assistant stopped after " + step + " steps without an answer: ask it"
                                + " a narrower question.");
                return;
            }
            ModelReply reply = model.handle(new AskModelQuery(new ModelRequest(
                    shown(told.build(history.handle(new GetHistoryQuery(conversation))), pictures),
                    tools(),
                    conversation.value(),
                    run.value())));
            usage.handle(new RecordRunStepCommand(run, new RunUsage(1, reply.tokensIn(), reply.tokensOut())));
            if (!reply.text().isBlank()) {
                steps.message(conversation, MessageRole.ASSISTANT, MessageKind.TEXT, reply.text(), null);
            }
            if (reply.toolCalls().isEmpty()) {
                steps.end(run, RunStatus.DONE);
                return;
            }
            for (ModelToolCall call : reply.toolCalls()) {
                if (call.name().equals(PlanProposer.TOOL) && proposePlan(conversation, call, context)) {
                    steps.end(run, RunStatus.WAITING_APPROVAL);
                    return;
                }
                if (!call.name().equals(PlanProposer.TOOL)) {
                    read(conversation, call, context, pictures);
                }
            }
        }
    }

    /** Whether the plan holds: then it waits for the editor; else the model is told why. */
    private boolean proposePlan(ConversationId conversation, ModelToolCall call, ToolContext context) {
        PlanProposer.Proposal proposal;
        try {
            proposal = plans.check(call.arguments(), context);
        } catch (RuntimeException e) {
            toolCall(conversation, call, "Error: " + e.getMessage());
            return false;
        }
        PlanId plan = propose.handle(new ProposePlanCommand(conversation, proposal.title(), proposal.actions()));
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("callId", call.id());
        payload.put("planId", plan.value().toString());
        payload.put("arguments", call.arguments());
        steps.message(
                conversation,
                MessageRole.ASSISTANT,
                MessageKind.PLAN,
                proposal.title().value(),
                payload);
        return true;
    }

    private void read(
            ConversationId conversation, ModelToolCall call, ToolContext context, Map<String, ModelMessage> pictures) {
        String result;
        try {
            Object value = calls.read(call.name(), call.arguments(), context);
            if (value instanceof ViewedImage image) {
                pictures.put(
                        call.id(),
                        ModelMessage.images(
                                "The picture " + call.name() + " showed:",
                                List.of(new ModelImage(image.mediaType(), image.data()))));
                Map<String, Object> seen = new LinkedHashMap<>();
                seen.put("mediaType", image.mediaType());
                seen.put("bytes", image.data().length() * 3L / 4);
                seen.put("note", "The picture is shown in the next message, in this run only.");
                result = steps.json(seen);
            } else {
                result = steps.json(value);
            }
        } catch (RuntimeException e) {
            result = "Error: " + e.getMessage();
        }
        toolCall(conversation, call, result);
    }

    private void toolCall(ConversationId conversation, ModelToolCall call, String result) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("callId", call.id());
        payload.put("tool", call.name());
        payload.put("arguments", call.arguments());
        payload.put("result", result);
        steps.message(conversation, MessageRole.ASSISTANT, MessageKind.TOOL_CALL, "", payload);
    }

    /** The history with each picture of this run right after the result of the call that showed it. */
    private static List<ModelMessage> shown(List<ModelMessage> history, Map<String, ModelMessage> pictures) {
        if (pictures.isEmpty()) {
            return history;
        }
        List<ModelMessage> messages = new ArrayList<>();
        for (ModelMessage message : history) {
            messages.add(message);
            if (message.role() == ModelRole.TOOL && pictures.containsKey(message.toolCallId())) {
                messages.add(pictures.get(message.toolCallId()));
            }
        }
        return messages;
    }

    private List<ModelTool> tools() {
        List<ModelTool> tools = new ArrayList<>();
        for (ReadTool<?> tool : registry.readTools()) {
            tools.add(new ModelTool(tool.name(), tool.description(), tool.schema()));
        }
        tools.add(plans.tool());
        return tools;
    }
}
