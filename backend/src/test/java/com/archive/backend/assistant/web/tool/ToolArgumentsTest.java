package com.archive.backend.assistant.web.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.junit.jupiter.api.Test;

/** The arguments a model writes for a tool, checked against the tool's schema and read into its record. */
class ToolArgumentsTest {

    private final ToolArguments arguments = new ToolArguments(new ObjectMapper());

    @Test
    void readsTheGivenFieldsIntoTheRecord() {
        Sample sample = arguments.parse(new SampleTool(), """
                {"phrase": "elite", "limit": 5, "kinds": ["author", "group"],
                 "filter": {"countryId": "00000000-0000-0000-0000-000000000001", "year": 1987}}
                """);

        assertThat(sample.phrase()).isEqualTo("elite");
        assertThat(sample.limit()).isEqualTo(5);
        assertThat(sample.kinds()).containsExactly("author", "group");
        assertThat(sample.filter())
                .isEqualTo(new Filter(UUID.fromString("00000000-0000-0000-0000-000000000001"), 1987));
    }

    @Test
    void readsAFieldLeftOutOrGivenAsNullAsNotGiven() {
        Sample sample = arguments.parse(new SampleTool(), """
                {"phrase": "elite", "limit": null}
                """);

        assertThat(sample.limit()).isNull();
        assertThat(sample.kinds()).isNull();
        assertThat(sample.filter()).isNull();
    }

    @Test
    void refusesAFieldTheSchemaDoesNotHave() {
        assertThatThrownBy(() -> arguments.parse(new SampleTool(), """
                        {"phrase": "elite", "title": "Elite"}
                        """))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("title");
    }

    @Test
    void refusesARequiredFieldLeftOut() {
        assertThatThrownBy(() -> arguments.parse(new SampleTool(), "{}"))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("phrase");
    }

    @Test
    void refusesAValueOfAnotherTypeNamingItsPath() {
        assertThatThrownBy(() -> arguments.parse(new SampleTool(), """
                        {"phrase": "elite", "filter": {"year": "1987"}}
                        """))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("filter.year");
    }

    @Test
    void refusesAValueOutsideAClosedList() {
        assertThatThrownBy(() -> arguments.parse(new SampleTool(), """
                        {"phrase": "elite", "kinds": ["software", "planet"]}
                        """))
                .isInstanceOf(ToolArgumentsException.class)
                .hasMessageContaining("kinds");
    }

    @Test
    void refusesWhatIsNotAJsonObject() {
        assertThatThrownBy(() -> arguments.parse(new SampleTool(), "[1, 2]"))
                .isInstanceOf(ToolArgumentsException.class);
        assertThatThrownBy(() -> arguments.parse(new SampleTool(), "{\"phrase\": "))
                .isInstanceOf(ToolArgumentsException.class);
    }

    @Test
    void readsNoArgumentsFromAnEmptyObject() {
        assertThat(arguments.parse(new NoArgumentsTool(), "{}")).isEqualTo(new NoArguments());
        assertThatThrownBy(() -> arguments.parse(new NoArgumentsTool(), "{\"id\": 1}"))
                .isInstanceOf(ToolArgumentsException.class);
    }

    record Sample(
            String phrase,
            @Nullable Integer limit,
            @Nullable List<String> kinds,
            @Nullable Filter filter) {}

    record Filter(@Nullable UUID countryId, @Nullable Integer year) {}

    static final class SampleTool implements Tool<Sample> {

        @Override
        public String name() {
            return "sample";
        }

        @Override
        public String description() {
            return "A sample tool.";
        }

        @Override
        public String schema() {
            return """
                    {
                      "type": "object",
                      "properties": {
                        "phrase": {"type": "string"},
                        "limit": {"type": ["integer", "null"]},
                        "kinds": {"type": ["array", "null"], "items": {"enum": ["software", "author", "group"]}},
                        "filter": {
                          "type": ["object", "null"],
                          "properties": {
                            "countryId": {"type": ["string", "null"], "format": "uuid"},
                            "year": {"type": ["integer", "null"]}
                          },
                          "additionalProperties": false
                        }
                      },
                      "required": ["phrase"],
                      "additionalProperties": false
                    }
                    """;
        }

        @Override
        public Class<Sample> argumentsType() {
            return Sample.class;
        }
    }

    static final class NoArgumentsTool implements Tool<NoArguments> {

        @Override
        public String name() {
            return "none";
        }

        @Override
        public String description() {
            return "Takes nothing.";
        }

        @Override
        public String schema() {
            return NoArguments.SCHEMA;
        }

        @Override
        public Class<NoArguments> argumentsType() {
            return NoArguments.class;
        }
    }
}
