package com.archive.backend.assistant.web.chat;

import com.archive.backend.assistant.app.chat.GetPlanHandler;
import com.archive.backend.assistant.app.chat.GetPlanQuery;
import com.archive.backend.assistant.app.chat.QueueRunCommand;
import com.archive.backend.assistant.app.chat.QueueRunHandler;
import com.archive.backend.assistant.app.chat.RecordPlanStepCommand;
import com.archive.backend.assistant.app.chat.RecordPlanStepHandler;
import com.archive.backend.assistant.domain.chat.ConversationId;
import com.archive.backend.assistant.domain.chat.MessageKind;
import com.archive.backend.assistant.domain.chat.MessageRole;
import com.archive.backend.assistant.domain.chat.PlanId;
import com.archive.backend.assistant.domain.chat.PlanStep;
import com.archive.backend.assistant.domain.chat.RunId;
import com.archive.backend.assistant.domain.chat.RunStatus;
import com.archive.backend.assistant.read.chat.ChatPlan;
import com.archive.backend.assistant.read.chat.ChatPlanAction;
import com.archive.backend.assistant.web.tool.ToolCalls;
import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.assistant.web.tool.Written;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

/**
 * Makes an approved plan: each change in order as the editor, the ids earlier ones produced in place of their
 * references, stopping at the first that fails or before the next once the editor asks to stop. Each outcome is told
 * in the conversation. Unless stopped, the assistant then sums up what came of it.
 */
@Component
@RequiredArgsConstructor
public class PlanExecutor {

    private static final Logger log = LoggerFactory.getLogger(PlanExecutor.class);

    private final ChatSteps steps;
    private final GetPlanHandler plans;
    private final RecordPlanStepHandler record;
    private final QueueRunHandler queue;
    private final AgentRunner agent;
    private final ToolCalls calls;
    private final ObjectMapper json;

    public void run(RunId run, ConversationId conversation, PlanId planId, ToolContext context) {
        if (!steps.claim(run)) {
            return;
        }
        try {
            if (!make(run, conversation, planId, context)) {
                return;
            }
        } catch (RuntimeException e) {
            log.error("Making a plan failed", e);
            steps.fail(run, conversation, "Making the plan failed: " + e.getMessage());
            return;
        }
        steps.end(run, RunStatus.DONE);
        agent.run(queue.handle(new QueueRunCommand(conversation)), conversation, context);
    }

    /** Whether the assistant is to sum it up: not when the editor stopped it. */
    private boolean make(RunId run, ConversationId conversation, PlanId planId, ToolContext context) {
        ChatPlan plan = plans.handle(new GetPlanQuery(planId));
        int size = plan.actions().size();
        Map<Integer, String> produced = new HashMap<>();
        for (ChatPlanAction action : plan.actions()) {
            int position = action.position();
            if (steps.stopRequested(run)) {
                record.handle(new RecordPlanStepCommand(planId, new PlanStep.Stopped()));
                progress(
                        conversation,
                        planId,
                        position,
                        "stopped",
                        "The editor stopped the plan before change " + position + " of " + size
                                + ": it and those after it were not made.");
                steps.stopped(run, conversation);
                return false;
            }
            record.handle(new RecordPlanStepCommand(planId, new PlanStep.Started(position)));
            String change = "Change " + position + " of " + size + " (" + action.tool() + ")";
            try {
                String arguments = PlanReferences.resolve(json.readTree(action.arguments()), produced)
                        .toString();
                Written written = calls.write(action.tool(), arguments, context);
                produced.put(position, written.id());
                record.handle(new RecordPlanStepCommand(planId, new PlanStep.Done(position, written.id())));
                progress(conversation, planId, position, "done", change + " done: " + written.id() + ".");
            } catch (JsonProcessingException | RuntimeException e) {
                String error = e.getMessage() == null ? e.getClass().getSimpleName() : e.getMessage();
                record.handle(new RecordPlanStepCommand(planId, new PlanStep.Failed(position, error)));
                progress(
                        conversation,
                        planId,
                        position,
                        "failed",
                        change + " failed: " + error
                                + " The changes before it are made; it and those after it were not.");
                return true;
            }
        }
        progress(conversation, planId, size, "done", "The plan \"" + plan.title() + "\" is done: every change made.");
        return true;
    }

    private void progress(ConversationId conversation, PlanId plan, int position, String status, String text) {
        Map<String, Object> payload = new LinkedHashMap<>();
        payload.put("planId", plan.value().toString());
        payload.put("position", position);
        payload.put("status", status);
        steps.message(conversation, MessageRole.SYSTEM, MessageKind.PLAN_PROGRESS, text, payload);
    }
}
