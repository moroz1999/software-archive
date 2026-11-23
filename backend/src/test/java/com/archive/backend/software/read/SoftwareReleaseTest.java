package com.archive.backend.software.read;

import static com.archive.backend.user.TestActors.ADDED;
import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** A release replaces whole groups of the hardware of its software (docs/domain/hardware.md). */
class SoftwareReleaseTest {

    private static final SoftwareHardware VECTOR =
            new SoftwareHardware("vector06c", "Vector-06C", "computers", "Computers", 1);
    private static final SoftwareHardware TAPE = new SoftwareHardware("tape", "Tape", "storage", "Storage", 2);
    private static final SoftwareHardware BEEPER = new SoftwareHardware("beeper", "Beeper", "sound", "Sound", 4);
    private static final SoftwareHardware AY = new SoftwareHardware("ay8910", "AY-3-8910", "sound", "Sound", 4);
    private static final SoftwareHardware JOYSTICK =
            new SoftwareHardware("joystick", "Joystick", "controls", "Controls", 5);
    private static final SoftwareHardware MICRODOS = new SoftwareHardware("microdos", "MicroDOS", "dos", "DOS", 3);

    private static final List<SoftwareHardware> BASE = List.of(VECTOR, TAPE, BEEPER, JOYSTICK);

    @Test
    void showsTheHardwareOfItsSoftwareWhenItHasNone() {
        assertThat(release(List.of()).over(BASE).hardware()).containsExactly(VECTOR, TAPE, BEEPER, JOYSTICK);
    }

    @Test
    void replacesTheWholeGroupItHasAndKeepsTheOthersOfItsSoftware() {
        assertThat(release(List.of(AY)).over(BASE).hardware()).containsExactly(VECTOR, TAPE, AY, JOYSTICK);
    }

    @Test
    void addsAGroupItsSoftwareDoesNotHaveInTheOrderOfTheGroups() {
        assertThat(release(List.of(MICRODOS)).over(BASE).hardware())
                .containsExactly(VECTOR, TAPE, MICRODOS, BEEPER, JOYSTICK);
    }

    @Test
    void keepsEverythingElseOfTheRelease() {
        SoftwareRelease release = release(List.of(AY));

        assertThat(release.over(BASE))
                .usingRecursiveComparison()
                .ignoringFields("hardware")
                .isEqualTo(release);
    }

    private static SoftwareRelease release(List<SoftwareHardware> own) {
        return new SoftwareRelease(UUID.randomUUID(), "Elite", "1.1", "mod", null, null, own, ADDED);
    }
}
