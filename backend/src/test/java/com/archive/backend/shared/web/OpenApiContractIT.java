package com.archive.backend.shared.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.archive.backend.IntegrationTest;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.web.servlet.MockMvc;

/**
 * The REST contract the frontend is typed from: the OpenAPI document of the backend, kept in the frontend sources.
 * Run with {@code -Dopenapi.write=true} (as {@code make api-types} does) to write the current document there.
 */
@IntegrationTest
class OpenApiContractIT {

    private static final Path FRONTEND_SPEC = Path.of("../frontend/src/shared/api/openapi.json");

    @Autowired
    private MockMvc mvc;

    @Autowired
    private ObjectMapper json;

    @Test
    void everyFieldIsSentAndOnlyOneMarkedNullableMayBeNull() throws Exception {
        JsonNode details = schemas().path("SoftwareDetailsDto");

        assertThat(texts(details.path("required"))).contains("id", "title", "canChange", "added", "description");
        assertThat(texts(details.at("/properties/description/type"))).containsExactlyInAnyOrder("string", "null");
        assertThat(details.at("/properties/title/type").asText()).isEqualTo("string");
    }

    @Test
    void recordsOfTheSameNameInDifferentDtosStayDifferentSchemas() throws Exception {
        String added = details("added");
        String origin = details("origins", "items");

        assertThat(texts(schemas().path(added).path("properties").fieldNames())).containsExactlyInAnyOrder("by", "at");
        assertThat(texts(schemas().path(origin).path("properties").fieldNames()))
                .containsExactlyInAnyOrder("origin", "externalId", "url");
    }

    @Test
    void theFrontendIsTypedFromTheCurrentContract() throws Exception {
        String current = json.writerWithDefaultPrettyPrinter().writeValueAsString(document()) + "\n";
        if (Boolean.getBoolean("openapi.write")) {
            Files.writeString(FRONTEND_SPEC, current, StandardCharsets.UTF_8);
        }

        assertThat(FRONTEND_SPEC)
                .as("the OpenAPI document in the frontend is stale: run `make api-types`")
                .exists();
        assertThat(Files.readString(FRONTEND_SPEC, StandardCharsets.UTF_8))
                .as("the OpenAPI document in the frontend is stale: run `make api-types`")
                .isEqualTo(current);
    }

    private String details(String... path) throws Exception {
        JsonNode node = schemas().path("SoftwareDetailsDto").path("properties");
        for (String step : path) {
            node = node.path(step);
        }
        String ref =
                node.path("$ref").asText(node.path("allOf").path(0).path("$ref").asText());
        return ref.substring(ref.lastIndexOf('/') + 1);
    }

    private JsonNode schemas() throws Exception {
        return document().path("components").path("schemas");
    }

    private JsonNode document() throws Exception {
        String body = mvc.perform(get("/v3/api-docs"))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString(StandardCharsets.UTF_8);
        return json.readTree(body);
    }

    private static List<String> texts(JsonNode array) {
        List<String> values = new ArrayList<>();
        array.forEach(value -> values.add(value.asText()));
        return values;
    }

    private static List<String> texts(Iterator<String> names) {
        List<String> values = new ArrayList<>();
        names.forEachRemaining(values::add);
        return values;
    }
}
