package com.archive.backend.group.app;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import org.junit.jupiter.api.Test;

class ListGroupTypesHandlerTest {

    @Test
    void returnsTheTypesOfTheReferenceBook() {
        ListGroupTypesHandler handler = new ListGroupTypesHandler(() -> List.of("demogroup", "label"));

        assertThat(handler.handle(new ListGroupTypesQuery())).containsExactly("demogroup", "label");
    }
}
