package com.archive.backend.group.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.RuleViolationException;
import java.util.Set;
import org.junit.jupiter.api.Test;

class GroupTypesTest {

    private static final GroupTypeCode DEMOGROUP = new GroupTypeCode("demogroup");
    private static final GroupTypeCode LABEL = new GroupTypeCode("label");

    private final GroupTypes groupTypes =
            new GroupTypes(codes -> Set.of(DEMOGROUP, LABEL).containsAll(codes));

    @Test
    void buildsTheTypesOfTheReferenceBook() {
        assertThat(groupTypes.of(Set.of(DEMOGROUP, LABEL)).codes()).containsExactlyInAnyOrder(DEMOGROUP, LABEL);
    }

    @Test
    void needsAType() {
        assertThatThrownBy(() -> groupTypes.of(Set.of())).isInstanceOfSatisfying(RuleViolationException.class, e -> {
            assertThat(e.code()).isEqualTo("group.types-required");
            assertThat(e.field()).isEqualTo("types");
        });
    }

    @Test
    void refusesATypeOutsideTheReferenceBook() {
        assertThatThrownBy(() -> groupTypes.of(Set.of(DEMOGROUP, new GroupTypeCode("orchestra"))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("group.type-unknown");
                    assertThat(e.field()).isEqualTo("types");
                });
    }

    @Test
    void restoresTheTypesAsStored() {
        assertThat(GroupTypeSet.restore(Set.of(LABEL))).isEqualTo(groupTypes.of(Set.of(LABEL)));
    }
}
