package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Set;
import org.junit.jupiter.api.Test;

/** Where the work comes from (docs/domain/software.md#lineage). */
class SoftwareLineageTest {

    @Test
    void refusesAReliabilityWithoutAKind() {
        assertThatThrownBy(() -> new SoftwareLineage(null, LineageReliability.RELIABLE, Set.of(), null))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-kind-required"));
    }

    @Test
    void refusesAReworkWithoutAKind() {
        assertThatThrownBy(() -> new SoftwareLineage(null, null, Set.of(LineageRework.NEW_LEVELS_ADDED), null))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-kind-required"));
    }

    @Test
    void isTheOriginalAloneWithoutAKind() {
        SoftwareLineage lineage = new SoftwareLineage(null, null, Set.of(), new LineageSource("Artillery, IBM PC"));

        assertThat(lineage.source()).isEqualTo(new LineageSource("Artillery, IBM PC"));
    }

    @Test
    void refusesABlankOriginal() {
        assertThatThrownBy(() -> new LineageSource("  "))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-source-invalid"));
    }

    @Test
    void takesAnOriginalOf255CharactersAndRefusesALongerOne() {
        assertThat(new LineageSource("a".repeat(255)).value()).hasSize(255);
        assertThatThrownBy(() -> new LineageSource("a".repeat(256)))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-source-invalid"));
    }

    @Test
    void knowsEveryKindReliabilityAndReworkByItsCode() {
        assertThat(LineageKind.fromCode("port-with-changes")).isEqualTo(LineageKind.PORT_WITH_CHANGES);
        assertThat(LineageKind.fromCode("new-levels")).isEqualTo(LineageKind.NEW_LEVELS);
        assertThat(LineageReliability.fromCode("assumption")).isEqualTo(LineageReliability.ASSUMPTION);
        assertThat(LineageRework.fromCode("graphics-and-sound-changed"))
                .isEqualTo(LineageRework.GRAPHICS_AND_SOUND_CHANGED);
    }

    @Test
    void refusesAnUnknownKindReliabilityOrRework() {
        assertThatThrownBy(() -> LineageKind.fromCode("conversion"))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-kind-invalid"));
        assertThatThrownBy(() -> LineageReliability.fromCode("unknown"))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-reliability-invalid"));
        assertThatThrownBy(() -> LineageRework.fromCode("sound-added"))
                .isInstanceOfSatisfying(
                        InvalidValueException.class,
                        e -> assertThat(e.code()).isEqualTo("software.lineage-rework-invalid"));
    }
}
