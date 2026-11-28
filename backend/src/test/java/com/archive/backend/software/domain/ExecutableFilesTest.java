package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import org.junit.jupiter.api.Test;

class ExecutableFilesTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");

    private static final PlatformCode BK = new PlatformCode("bk");

    @Test
    void aVectorRomIsExecutableWhateverTheCaseOfItsExtension() {
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("ELITE.ROM")))
                .isTrue();
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("elite.rom")))
                .isTrue();
    }

    @Test
    void aVectorComR0mAndVecAreExecutable() {
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("XZONE.COM")))
                .isTrue();
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("game.r0m")))
                .isTrue();
        assertThat(ExecutableFiles.isExecutable(VECTOR, new ReleaseFileName("demo.Vec")))
                .isTrue();
    }

    @Test
    void aRomIsNotExecutableOnAPlatformWithoutThatExtension() {
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("ELITE.ROM")))
                .isFalse();
    }

    @Test
    void theBinaryFilesOfBkAreExecutableWhateverTheCaseOfTheirExtension() {
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("LPUFF.BIN")))
                .isTrue();
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("module1.ovl")))
                .isTrue();
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("Cave.Gme")))
                .isTrue();
    }

    @Test
    void theBasicAndFocalProgramsAndTheMediaOfBkAreNotExecutable() {
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("ARTIL.COD")))
                .isFalse();
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("calc.foc")))
                .isFalse();
        assertThat(ExecutableFiles.isExecutable(BK, new ReleaseFileName("GAMES.BKD")))
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
