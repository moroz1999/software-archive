package com.archive.backend.assistant.infra;

import com.archive.backend.assistant.domain.chat.ModelFailedException;
import com.archive.backend.assistant.read.model.LanguageModel;
import com.archive.backend.assistant.read.model.ModelImage;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelReply;
import com.archive.backend.assistant.read.model.ModelRequest;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelTool;
import com.archive.backend.assistant.read.model.ModelToolCall;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import java.io.IOException;
import java.time.Duration;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;
import okhttp3.ResponseBody;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.stereotype.Component;

/**
 * The OpenAI Responses API with function tools, through OkHttp. Nothing is stored on OpenAI's side: every request
 * carries the whole conversation. Every exchange is logged under the logger {@code assistant.exchanges}, as what is
 * new in it — for the first request of a run what came after the model's last turn, for a later one what was added
 * since the previous — pictures as their type and size; then the response as it came, or the failure.
 */
@Component
@EnableConfigurationProperties(OpenAiProperties.class)
public class OpenAiLanguageModel implements LanguageModel {

    private static final Logger log = LoggerFactory.getLogger("assistant.exchanges");
    private static final MediaType JSON = MediaType.get("application/json");
    private static final int REMEMBERED_RUNS = 100;

    private final OpenAiProperties properties;
    private final ObjectMapper json;
    private final OkHttpClient client = new OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(15))
            .readTimeout(Duration.ofMinutes(3))
            .callTimeout(Duration.ofMinutes(4))
            .build();

    /** How many input items of each run are logged already. */
    private final Map<UUID, Integer> logged = Collections.synchronizedMap(new LinkedHashMap<>() {
        @Override
        protected boolean removeEldestEntry(Map.Entry<UUID, Integer> eldest) {
            return size() > REMEMBERED_RUNS;
        }
    });

    public OpenAiLanguageModel(OpenAiProperties properties, ObjectMapper json) {
        this.properties = properties;
        this.json = json;
    }

    @Override
    public ModelReply complete(ModelRequest request) {
        if (properties.apiKey() == null || properties.apiKey().isBlank()) {
            throw new ModelFailedException("The assistant is not configured: OPENAI_API_KEY is not set.");
        }
        ArrayNode input = json.createArrayNode();
        request.messages().forEach(message -> items(message, input));
        ObjectNode body = json.createObjectNode();
        body.put("model", properties.model());
        body.put("store", false);
        body.set("input", input);
        if (!request.tools().isEmpty()) {
            ArrayNode tools = body.putArray("tools");
            request.tools().forEach(tool -> tools.add(tool(tool)));
        }
        String prefix = "conversation " + request.conversationId() + " run " + request.runId() + " ";
        logRequest(prefix, request, input);
        long started = System.nanoTime();
        try (Response response = client.newCall(new Request.Builder()
                        .url(properties.baseUrl().replaceAll("/+$", "") + "/responses")
                        .header("Authorization", "Bearer " + properties.apiKey())
                        .post(RequestBody.create(write(body), JSON))
                        .build())
                .execute()) {
            ResponseBody responseBody = response.body();
            String text = responseBody == null ? "" : responseBody.string();
            log.info("{}response {} after {} ms {}", prefix, response.code(), elapsed(started), withoutTools(text));
            JsonNode answer = read(text);
            if (!response.isSuccessful()) {
                throw new ModelFailedException("The model refused the request: HTTP " + response.code() + ": "
                        + answer.path("error").path("message").asText(text));
            }
            if (answer.path("status").asText().equals("failed")
                    || answer.path("error").isObject()) {
                throw new ModelFailedException("The model failed: "
                        + answer.path("error").path("message").asText("no reason given"));
            }
            return reply(answer);
        } catch (IOException e) {
            log.warn("{}failed after {} ms: {}", prefix, elapsed(started), e.getMessage());
            throw new ModelFailedException("The model could not be asked: " + e.getMessage());
        }
    }

    private void items(ModelMessage message, ArrayNode input) {
        if (message.role() == ModelRole.TOOL) {
            input.addObject()
                    .put("type", "function_call_output")
                    .put("call_id", message.toolCallId())
                    .put("output", message.text());
            return;
        }
        String role = message.role().name().toLowerCase(Locale.ROOT);
        if (!message.images().isEmpty()) {
            ObjectNode item = input.addObject().put("role", role);
            ArrayNode content = item.putArray("content");
            content.addObject().put("type", "input_text").put("text", message.text());
            for (ModelImage image : message.images()) {
                content.addObject()
                        .put("type", "input_image")
                        .put("image_url", "data:" + image.mediaType() + ";base64," + image.data());
            }
        } else if (!message.text().isEmpty() || message.toolCalls().isEmpty()) {
            input.addObject().put("role", role).put("content", message.text());
        }
        for (ModelToolCall call : message.toolCalls()) {
            input.addObject()
                    .put("type", "function_call")
                    .put("call_id", call.id())
                    .put("name", call.name())
                    .put("arguments", call.arguments());
        }
    }

    private ObjectNode tool(ModelTool tool) {
        ObjectNode node = json.createObjectNode()
                .put("type", "function")
                .put("name", tool.name())
                .put("description", tool.description());
        node.set("parameters", read(tool.schema()));
        return node;
    }

    private static ModelReply reply(JsonNode answer) {
        StringBuilder text = new StringBuilder();
        List<ModelToolCall> calls = new ArrayList<>();
        for (JsonNode item : answer.path("output")) {
            switch (item.path("type").asText()) {
                case "message" -> {
                    for (JsonNode content : item.path("content")) {
                        if (content.path("type").asText().equals("output_text")) {
                            text.append(content.path("text").asText());
                        }
                    }
                }
                case "function_call" ->
                    calls.add(new ModelToolCall(
                            item.path("call_id").asText(),
                            item.path("name").asText(),
                            item.path("arguments").asText()));
                default -> {
                    // reasoning and the like: logged, not told
                }
            }
        }
        JsonNode usage = answer.path("usage");
        return new ModelReply(
                text.toString(),
                calls,
                usage.path("input_tokens").asLong(),
                usage.path("output_tokens").asLong());
    }

    private void logRequest(String prefix, ModelRequest request, ArrayNode input) {
        int count = input.size();
        Integer before = logged.put(request.runId(), count);
        int from = before != null ? before : afterModelsLastTurn(input);
        ArrayNode fresh = json.createArrayNode();
        for (int i = from; i < count; i++) {
            fresh.add(withoutImages(input.get(i).deepCopy()));
        }
        log.info(
                "{}request {} input[{}..{}] of {}, tools: {} {}",
                prefix,
                properties.model(),
                from,
                count - 1,
                count,
                String.join(", ", request.tools().stream().map(ModelTool::name).toList()),
                fresh);
    }

    private static int afterModelsLastTurn(ArrayNode input) {
        for (int i = input.size() - 1; i >= 0; i--) {
            JsonNode item = input.get(i);
            if (item.path("role").asText().equals("assistant") || item.has("type")) {
                return i + 1;
            }
        }
        return 0;
    }

    private static JsonNode withoutImages(JsonNode node) {
        if (node instanceof ObjectNode object) {
            JsonNode url = object.get("image_url");
            if (url != null && url.asText().startsWith("data:")) {
                String value = url.asText();
                int comma = value.indexOf(',');
                String type = value.substring(5, Math.max(5, value.indexOf(';')));
                object.put("image_url", "<" + type + ", " + (value.length() - comma - 1) * 3 / 4 + " bytes>");
            }
            object.forEach(OpenAiLanguageModel::withoutImages);
        } else if (node instanceof ArrayNode array) {
            array.forEach(OpenAiLanguageModel::withoutImages);
        }
        return node;
    }

    private String withoutTools(String text) {
        JsonNode node = readQuietly(text);
        if (node instanceof ObjectNode object) {
            object.remove("tools");
            return object.toString();
        }
        return text;
    }

    private JsonNode read(String text) {
        JsonNode node = readQuietly(text);
        return node == null ? json.createObjectNode() : node;
    }

    private @org.jspecify.annotations.Nullable JsonNode readQuietly(String text) {
        try {
            return text.isBlank() ? null : json.readTree(text);
        } catch (JsonProcessingException e) {
            return null;
        }
    }

    private String write(JsonNode node) {
        try {
            return json.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new IllegalStateException("The request cannot be written as JSON", e);
        }
    }

    private static long elapsed(long started) {
        return Duration.ofNanos(System.nanoTime() - started).toMillis();
    }
}
