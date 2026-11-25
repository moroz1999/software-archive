package com.archive.backend.assistant;

import com.archive.backend.assistant.read.model.LanguageModel;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelReply;
import com.archive.backend.assistant.read.model.ModelRequest;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelToolCall;
import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Deque;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/**
 * The model of the tests: it answers each request with the next reply it was given, and remembers every request.
 * With no reply left it answers "Done.".
 */
public class ScriptedLanguageModel implements LanguageModel {

    private final Deque<Function<ModelRequest, ModelReply>> replies = new ArrayDeque<>();
    private final List<ModelRequest> requests = new ArrayList<>();
    private Function<ModelRequest, ModelReply> otherwise = request -> text("Done.");

    @Override
    public synchronized ModelReply complete(ModelRequest request) {
        requests.add(request);
        Function<ModelRequest, ModelReply> next = replies.poll();
        return (next == null ? otherwise : next).apply(request);
    }

    /** Forgets what it was given and asked: before each test. */
    public synchronized void reset() {
        replies.clear();
        requests.clear();
        otherwise = request -> text("Done.");
    }

    /** Answers the next request with prose. */
    public ScriptedLanguageModel replies(String text) {
        return then(request -> text(text));
    }

    /** Answers the next request with a call of the tool. */
    public ScriptedLanguageModel calls(String tool, String arguments) {
        return then(request -> call(tool, arguments));
    }

    public synchronized ScriptedLanguageModel then(Function<ModelRequest, ModelReply> reply) {
        replies.add(reply);
        return this;
    }

    /** Answers every request after the scripted ones so. */
    public synchronized ScriptedLanguageModel always(Function<ModelRequest, ModelReply> reply) {
        otherwise = reply;
        return this;
    }

    public synchronized List<ModelRequest> requests() {
        return List.copyOf(requests);
    }

    public ModelRequest lastRequest() {
        List<ModelRequest> all = requests();
        return all.get(all.size() - 1);
    }

    public static ModelReply text(String text) {
        return new ModelReply(text, List.of(), 10, 5);
    }

    public static ModelReply call(String tool, String arguments) {
        return new ModelReply("", List.of(new ModelToolCall("call-" + UUID.randomUUID(), tool, arguments)), 10, 5);
    }

    /** The result the request carries of the call last made. */
    public static String lastResult(ModelRequest request) {
        List<ModelMessage> messages = request.messages();
        for (int i = messages.size() - 1; i >= 0; i--) {
            if (messages.get(i).role() == ModelRole.TOOL) {
                return messages.get(i).text();
            }
        }
        throw new AssertionError("No result of a call in the request");
    }
}
