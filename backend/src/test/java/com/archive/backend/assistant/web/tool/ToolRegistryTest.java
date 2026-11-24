package com.archive.backend.assistant.web.tool;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.junit.jupiter.api.Test;

/** The tools of the assistant, found by their names. */
class ToolRegistryTest {

    @Test
    void findsAToolByItsName() {
        ReadTool<NoArguments> first = new Named("first");
        ToolRegistry registry = new ToolRegistry(List.of(first, new Named("second")));

        assertThat(registry.tool("first")).isSameAs(first);
    }

    @Test
    void listsTheToolsInTheOrderGiven() {
        ToolRegistry registry = new ToolRegistry(List.of(new Named("b"), new Named("a")));

        assertThat(registry.tools()).extracting(Tool::name).containsExactly("b", "a");
        assertThat(registry.readTools()).extracting(Tool::name).containsExactly("b", "a");
    }

    @Test
    void refusesANameNoToolHas() {
        ToolRegistry registry = new ToolRegistry(List.of(new Named("first")));

        assertThatThrownBy(() -> registry.tool("third"))
                .isInstanceOf(UnknownToolException.class)
                .hasMessageContaining("third");
    }

    @Test
    void refusesTwoToolsOfOneName() {
        assertThatThrownBy(() -> new ToolRegistry(List.of(new Named("first"), new Named("first"))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("first");
    }

    private record Named(String name) implements ReadTool<NoArguments> {

        @Override
        public String description() {
            return "A tool named " + name;
        }

        @Override
        public String schema() {
            return NoArguments.SCHEMA;
        }

        @Override
        public Class<NoArguments> argumentsType() {
            return NoArguments.class;
        }

        @Override
        public Object read(ToolContext context, NoArguments arguments) {
            return name;
        }
    }
}
