package com.archive.backend.importer.domain.vektor;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.HardwareCode;
import java.util.List;
import org.junit.jupiter.api.Test;

class VektorStorageTest {

    private static final HardwareCode TAPE = new HardwareCode("tape");
    private static final HardwareCode FLOPPY = new HardwareCode("floppy");

    @Test
    void takesADiskImageForAFloppy() {
        assertThat(VektorStorage.of(List.of("robocop.fdd"))).containsExactly(FLOPPY);
    }

    @Test
    void takesRomFilesAndTheirRecordingsForATape() {
        assertThat(VektorStorage.of(List.of("KOLOBEG.ROM"))).containsExactly(TAPE);
        assertThat(VektorStorage.of(List.of("512demo.r0m", "512demo.asm"))).containsExactly(TAPE);
        assertThat(VektorStorage.of(List.of("A09-CopyA.wav"))).containsExactly(TAPE);
    }

    @Test
    void takesBothWhenTheArchiveHoldsBoth() {
        assertThat(VektorStorage.of(List.of("cpm.fdd", "CPM39.ROM", "cpm39.txt")))
                .containsExactlyInAnyOrder(FLOPPY, TAPE);
    }

    @Test
    void knowsNoStorageFromOtherFiles() {
        assertThat(VektorStorage.of(List.of("mdbold21.com", "readme.txt"))).isEmpty();
        assertThat(VektorStorage.of(List.of())).isEmpty();
    }
}
