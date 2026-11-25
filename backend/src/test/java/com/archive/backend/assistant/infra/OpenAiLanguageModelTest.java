package com.archive.backend.assistant.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.assistant.domain.chat.ModelFailedException;
import com.archive.backend.assistant.read.model.ModelImage;
import com.archive.backend.assistant.read.model.ModelMessage;
import com.archive.backend.assistant.read.model.ModelReply;
import com.archive.backend.assistant.read.model.ModelRequest;
import com.archive.backend.assistant.read.model.ModelRole;
import com.archive.backend.assistant.read.model.ModelTool;
import com.archive.backend.assistant.read.model.ModelToolCall;
import com.archive.backend.remote.TestSite;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

class OpenAiLanguageModelTest {

    private static final String ANSWER = """
            {"id": "resp_1", "status": "completed", "error": null,
             "output": [
               {"type": "reasoning", "summary": []},
               {"type": "message", "role": "assistant", "content": [{"type": "output_text", "text": "Ищу."}]},
               {"type": "function_call", "call_id": "c9", "name": "search_software",
                "arguments": "{\\"query\\": \\"Elite\\"}"}
             ],
             "usage": {"input_tokens": 1200, "output_tokens": 34}}
            """;

    private final ObjectMapper json = new ObjectMapper();
    private final TestSite site = TestSite.start();

    @AfterEach
    void stop() {
        site.close();
    }

    @Test
    void sendsTheWholeConversationAndTheToolsToTheResponsesApi() throws Exception {
        site.serve("/v1/responses", 200, Map.of("Content-Type", "application/json"), bytes(ANSWER));

        model("sk-test")
                .complete(new ModelRequest(
                        List.of(
                                ModelMessage.of(ModelRole.SYSTEM, "You are the assistant."),
                                ModelMessage.of(ModelRole.USER, "Найди Elite"),
                                ModelMessage.calls(
                                        List.of(new ModelToolCall("c1", "search_software", "{\"query\": \"Elite\"}"))),
                                ModelMessage.result("c1", "{\"total\": 0}"),
                                ModelMessage.of(ModelRole.ASSISTANT, "Не нашёл.")),
                        List.of(new ModelTool(
                                "search_software",
                                "Finds software.",
                                "{\"type\": \"object\", \"properties\": {}, \"additionalProperties\": false}")),
                        UUID.randomUUID(),
                        UUID.randomUUID()));

        TestSite.Received request = site.received().get(0);
        assertThat(request.header("Authorization")).isEqualTo("Bearer sk-test");
        JsonNode body = json.readTree(request.body());
        assertThat(body.get("model").asText()).isEqualTo("gpt-test");
        assertThat(body.get("store").asBoolean()).isFalse();
        assertThat(body.get("input")).isEqualTo(json.readTree("""
                [
                  {"role": "system", "content": "You are the assistant."},
                  {"role": "user", "content": "Найди Elite"},
                  {"type": "function_call", "call_id": "c1", "name": "search_software",
                   "arguments": "{\\"query\\": \\"Elite\\"}"},
                  {"type": "function_call_output", "call_id": "c1", "output": "{\\"total\\": 0}"},
                  {"role": "assistant", "content": "Не нашёл."}
                ]
                """));
        assertThat(body.get("tools")).isEqualTo(json.readTree("""
                [{"type": "function", "name": "search_software", "description": "Finds software.",
                  "parameters": {"type": "object", "properties": {}, "additionalProperties": false}}]
                """));
    }

    @Test
    void showsAPictureAsAnImageOfTheMessage() throws Exception {
        site.serve("/v1/responses", 200, Map.of("Content-Type", "application/json"), bytes(ANSWER));

        model("sk-test")
                .complete(request(ModelMessage.images(
                        "The picture of view_image:", List.of(new ModelImage("image/png", "iVBORw0K")))));

        JsonNode input = json.readTree(site.received().get(0).body()).get("input");
        assertThat(input).isEqualTo(json.readTree("""
                [{"role": "user", "content": [
                   {"type": "input_text", "text": "The picture of view_image:"},
                   {"type": "input_image", "image_url": "data:image/png;base64,iVBORw0K"}]}]
                """));
    }

    @Test
    void readsTheProseTheCallsAndTheTokensOfTheAnswer() {
        site.serve("/v1/responses", 200, Map.of("Content-Type", "application/json"), bytes(ANSWER));

        ModelReply reply = model("sk-test").complete(request(ModelMessage.of(ModelRole.USER, "Найди Elite")));

        assertThat(reply)
                .isEqualTo(new ModelReply(
                        "Ищу.",
                        List.of(new ModelToolCall("c9", "search_software", "{\"query\": \"Elite\"}")),
                        1200,
                        34));
    }

    @Test
    void aRefusedRequestIsAFailureOfTheModelWithItsReason() {
        site.serve("/v1/responses", 429, Map.of("Content-Type", "application/json"), bytes("""
                {"error": {"message": "Rate limit reached", "type": "requests"}}
                """));

        assertThatThrownBy(() -> model("sk-test").complete(request(ModelMessage.of(ModelRole.USER, "Привет"))))
                .isInstanceOf(ModelFailedException.class)
                .hasMessageContaining("429")
                .hasMessageContaining("Rate limit reached");
    }

    @Test
    void anAnswerThatFailedIsAFailureOfTheModel() {
        site.serve("/v1/responses", 200, Map.of("Content-Type", "application/json"), bytes("""
                {"id": "resp_2", "status": "failed", "error": {"code": "server_error", "message": "Overloaded"},
                 "output": [], "usage": null}
                """));

        assertThatThrownBy(() -> model("sk-test").complete(request(ModelMessage.of(ModelRole.USER, "Привет"))))
                .isInstanceOf(ModelFailedException.class)
                .hasMessageContaining("Overloaded");
    }

    @Test
    void withoutAKeyTheAssistantIsNotConfiguredAndAsksNobody() {
        assertThatThrownBy(() -> model(" ").complete(request(ModelMessage.of(ModelRole.USER, "Привет"))))
                .isInstanceOf(ModelFailedException.class)
                .hasMessageContaining("OPENAI_API_KEY");
        assertThat(site.received()).isEmpty();
    }

    private OpenAiLanguageModel model(String key) {
        return new OpenAiLanguageModel(new OpenAiProperties(key, "gpt-test", site.url("/v1")), json);
    }

    private static ModelRequest request(ModelMessage message) {
        return new ModelRequest(List.of(message), List.of(), UUID.randomUUID(), UUID.randomUUID());
    }

    private static byte[] bytes(String text) {
        return text.getBytes(StandardCharsets.UTF_8);
    }
}
