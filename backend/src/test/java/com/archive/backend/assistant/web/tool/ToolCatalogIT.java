package com.archive.backend.assistant.web.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.TestActors;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.lang.reflect.RecordComponent;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.StreamSupport;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

/** The tools the assistant is offered, their schemas, and calls written as the model writes them. */
@IntegrationTest
class ToolCatalogIT {

    private static final ToolContext CONTEXT = new ToolContext(TestActors.MEMBER, Language.EN);

    @Autowired
    private ToolRegistry registry;

    @Autowired
    private ToolCalls calls;

    @Autowired
    private ObjectMapper json;

    @Test
    void offersEveryReadTool() {
        assertThat(registry.readTools())
                .extracting(Tool::name)
                .containsExactlyInAnyOrder(
                        "search",
                        "find_names",
                        "search_software",
                        "list_latest_software",
                        "list_authors",
                        "list_groups",
                        "search_releases",
                        "get_software",
                        "get_release",
                        "get_author",
                        "get_group",
                        "get_label",
                        "list_categories",
                        "list_platforms",
                        "list_hardware",
                        "list_countries",
                        "list_cities",
                        "list_options",
                        "load_skill");
    }

    @Test
    void everyToolSaysWhatItDoes() {
        assertThat(registry.tools())
                .allSatisfy(
                        tool -> assertThat(tool.description()).as(tool.name()).isNotBlank());
    }

    @Test
    void everySchemaIsAnObjectClosedAtEveryDepth() throws Exception {
        for (Tool<?> tool : registry.tools()) {
            JsonNode schema = json.readTree(tool.schema());
            assertThat(types(schema)).as(tool.name()).contains("object");
            List<String> open = new ArrayList<>();
            collectOpenObjects(schema, tool.name(), open);
            assertThat(open).as("objects without additionalProperties: false").isEmpty();
        }
    }

    @Test
    void everySchemaHasTheFieldsOfItsArgumentsRequiringThoseThatCannotBeLeftOut() throws Exception {
        for (Tool<?> tool : registry.tools()) {
            assertMatches(json.readTree(tool.schema()), tool.argumentsType(), tool.name());
        }
    }

    @Test
    void runsACallWrittenAsJson() throws Exception {
        Object result = calls.read("list_options", "{}", CONTEXT);

        JsonNode options = json.valueToTree(result);
        assertThat(texts(options.get("releaseTypes"))).contains("original", "crack");
        assertThat(texts(options.get("creditRoles"))).contains("developer", "music");
        assertThat(texts(options.get("memberRoles"))).contains("coder", "musician");
        assertThat(texts(options.get("preservationStatuses"))).contains("unknown", "missing-in-action");
        assertThat(texts(options.get("groupTypes"))).isNotEmpty();
    }

    @Test
    void refusesAToolNobodyHas() {
        assertThatThrownBy(() -> calls.read("delete_everything", "{}", CONTEXT))
                .isInstanceOf(UnknownToolException.class)
                .hasMessageContaining("delete_everything");
    }

    @Test
    void refusesArgumentsTheSchemaDoesNotTake() {
        assertThatThrownBy(() -> calls.read("search_releases", "{\"md5\": \"abc\"}", CONTEXT))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("md5");
        assertThatThrownBy(() -> calls.read("search_releases", "{\"name\": \"Elite\"}", CONTEXT))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("name");
        assertThatThrownBy(() -> calls.read("get_author", "{}", CONTEXT))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("id");
    }

    private static void assertMatches(JsonNode schema, Class<?> type, String path) {
        assertThat(type.isRecord()).as(path + " is read into a record").isTrue();
        RecordComponent[] components = type.getRecordComponents();
        Set<String> properties = new HashSet<>();
        schema.path("properties").fieldNames().forEachRemaining(properties::add);
        assertThat(properties)
                .as(path)
                .containsExactlyInAnyOrderElementsOf(
                        Arrays.stream(components).map(RecordComponent::getName).toList());
        Set<String> required = new HashSet<>(texts(schema.path("required")));
        for (RecordComponent component : components) {
            String name = path + "." + component.getName();
            JsonNode property = schema.path("properties").path(component.getName());
            boolean nullable = component.getAnnotatedType().isAnnotationPresent(Nullable.class);
            assertThat(required.contains(component.getName()))
                    .as(name + " required")
                    .isEqualTo(!nullable);
            assertThat(types(property).contains("null"))
                    .as(name + " takes null")
                    .isEqualTo(nullable);
            if (component.getType().isRecord()) {
                assertMatches(property, component.getType(), name);
            }
        }
    }

    private static void collectOpenObjects(JsonNode node, String path, List<String> open) {
        if (types(node).contains("object")
                && !(node.path("additionalProperties").isBoolean()
                        && !node.path("additionalProperties").asBoolean())) {
            open.add(path);
        }
        node.path("properties")
                .fields()
                .forEachRemaining(field -> collectOpenObjects(field.getValue(), path + "." + field.getKey(), open));
        if (node.has("items")) {
            collectOpenObjects(node.get("items"), path + "[]", open);
        }
    }

    private static List<String> types(JsonNode node) {
        JsonNode type = node.path("type");
        return type.isArray() ? texts(type) : type.isTextual() ? List.of(type.asText()) : List.of();
    }

    private static List<String> texts(JsonNode array) {
        return StreamSupport.stream(array.spliterator(), false)
                .map(JsonNode::asText)
                .toList();
    }
}
