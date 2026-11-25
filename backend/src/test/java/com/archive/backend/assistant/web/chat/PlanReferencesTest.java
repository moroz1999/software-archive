package com.archive.backend.assistant.web.chat;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.assistant.web.tool.ToolArgumentsException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
import org.junit.jupiter.api.Test;

class PlanReferencesTest {

    private final ObjectMapper json = new ObjectMapper();

    @Test
    void findsTheEarlierChangesAnIdIsTakenFromAtAnyDepth() throws Exception {
        JsonNode arguments = json.readTree("""
                {"softwareId": {"$ref": 1}, "credits": [{"labelId": {"$ref": 2}, "roles": ["code"]}],
                 "groupId": {"$ref": 1}, "title": "Elite"}
                """);

        assertThat(PlanReferences.of(arguments, 3)).containsExactly(1, 2);
    }

    @Test
    void argumentsWithoutReferencesTakeNothing() throws Exception {
        assertThat(PlanReferences.of(json.readTree("{\"title\": \"Elite\", \"ref\": 1}"), 1))
                .isEmpty();
    }

    @Test
    void onlyAnEarlierChangeIsReferredTo() throws Exception {
        assertThatThrownBy(() -> PlanReferences.of(json.readTree("{\"softwareId\": {\"$ref\": 2}}"), 2))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("earlier");
        assertThatThrownBy(() -> PlanReferences.of(json.readTree("{\"softwareId\": {\"$ref\": 3}}"), 2))
                .isInstanceOf(ToolArgumentsException.class);
    }

    @Test
    void aReferenceIsTheNumberOfAChangeAndNothingElse() throws Exception {
        for (String wrong : new String[] {
            "{\"softwareId\": {\"$ref\": \"1\"}}",
            "{\"softwareId\": {\"$ref\": 0}}",
            "{\"softwareId\": {\"$ref\": 1.5}}",
            "{\"softwareId\": {\"$ref\": 1, \"id\": \"x\"}}"
        }) {
            assertThatThrownBy(() -> PlanReferences.of(json.readTree(wrong), 3))
                    .as(wrong)
                    .isInstanceOf(ToolArgumentsException.class);
        }
    }

    @Test
    void putsTheIdsTheEarlierChangesProducedInPlaceOfTheirReferences() throws Exception {
        JsonNode arguments = json.readTree("""
                {"softwareId": {"$ref": 1}, "credits": [{"labelId": {"$ref": 2}}], "title": "Elite"}
                """);

        JsonNode resolved = PlanReferences.resolve(arguments, Map.of(1, "id-1", 2, "id-2"));

        assertThat(resolved).isEqualTo(json.readTree("""
                {"softwareId": "id-1", "credits": [{"labelId": "id-2"}], "title": "Elite"}
                """));
        assertThat(arguments.get("softwareId").has("$ref")).isTrue();
    }
}
