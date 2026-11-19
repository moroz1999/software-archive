package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The files that make a release of a platform (docs/domain/imports.md#software). */
class ProgramFilesTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");

    @Test
    void areTheRomFilesFloppyImagesAndTapeRecordingsOfVectorWhateverTheCaseOfTheirNames() {
        ReleaseFile root = zip(
                "bundle.zip",
                file("ELITE.ROM", "a"),
                directory("disks", file("game.Fdd", "b"), file("readme.txt", "c")),
                file("tape.cas", "d"),
                zip("nested.zip", file("SOUND.WAV", "e")));

        assertThat(ProgramFiles.of(VECTOR, root)).containsExactlyInAnyOrder(hash("a"), hash("b"), hash("d"), hash("e"));
    }

    @Test
    void doesNotLookIntoAProgramFile() {
        ReleaseFile root = zip("bundle.zip", zip("disk.fdd", file("INNER.ROM", "a")));

        assertThat(ProgramFiles.of(VECTOR, root)).containsExactly(hashOf("disk.fdd"));
    }

    @Test
    void isTheFileItselfWhenItIsAProgramFile() {
        assertThat(ProgramFiles.of(VECTOR, file("ELITE.ROM", "a"))).containsExactly(hash("a"));
    }

    @Test
    void isNothingOnAPlatformThatHasNoProgramFilesYet() {
        assertThat(ProgramFiles.of(new PlatformCode("bk"), zip("bundle.zip", file("ELITE.ROM", "a"))))
                .isEmpty();
    }

    private static ReleaseFile file(String name, String digest) {
        return new ReleaseFile(id(), new ReleaseFileName(name), hash(digest), false, false, false, List.of());
    }

    private static ReleaseFile zip(String name, ReleaseFile... children) {
        return new ReleaseFile(id(), new ReleaseFileName(name), hashOf(name), false, false, false, List.of(children));
    }

    private static ReleaseFile directory(String name, ReleaseFile... children) {
        return new ReleaseFile(id(), new ReleaseFileName(name), null, true, false, false, List.of(children));
    }

    /** A digest standing for the content it names. */
    private static ReleaseFileHash hash(String digest) {
        return new ReleaseFileHash(digest.repeat(32));
    }

    private static ReleaseFileHash hashOf(String name) {
        return new ReleaseFileHash(String.format("%032x", Math.abs((long) name.hashCode())));
    }

    private static ReleaseFileId id() {
        return new ReleaseFileId(UUID.randomUUID());
    }
}
