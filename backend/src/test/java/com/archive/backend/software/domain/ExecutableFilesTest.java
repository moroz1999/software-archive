package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import org.junit.jupiter.api.Test;

class ExecutableFilesTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");

    @Test
    void aVectorRomIsExecutableWhateverTheCaseOfItsExtension() {
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("ELITE.ROM")))
                .isTrue();
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("elite.rom")))
                .isTrue();
    }

    @Test
    void aRomIsNotExecutableOnAPlatformWithoutThatExtension() {
        assertThat(ExecutableFiles.isExecutable(new PlatformCode("bk"), new ReleaseFileName("ELITE.ROM")))
                .isFalse();
    }

    @Test
    void aFileWithoutAnExtensionIsNotExecutable() {
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("ROM")))
                .isFalse();
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("elite.")))
                .isFalse();
    }
}
