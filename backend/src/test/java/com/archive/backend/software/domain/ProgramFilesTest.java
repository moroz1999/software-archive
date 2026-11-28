package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

/** The files that make a release of a platform (docs/domain/imports.md#software). */
class ProgramFilesTest {

    private static final PlatformCode VECTOR = new PlatformCode("vector");

    private static final PlatformCode BK = new PlatformCode("bk");

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
    void areEveryExecutableFileOfVector() {
        ReleaseFile root = zip(
                "bundle.zip", file("XZONE.COM", "a"), file("game.r0m", "b"), file("demo.vec", "c"), file("a.txt", "d"));

        assertThat(ProgramFiles.of(VECTOR, root)).containsExactlyInAnyOrder(hash("a"), hash("b"), hash("c"));
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
    void areTheBinaryFilesFloppyImagesTapeRecordingsAndBasicAndFocalProgramsOfBkWhateverTheCaseOfTheirNames() {
        ReleaseFile root = zip(
                "bundle.zip",
                file("LPUFF.BIN", "a"),
                file("module1.ovl", "b"),
                file("LITTLE.GME", "c"),
                directory("disks", file("GAMES.BKD", "d"), file("andos.img", "e"), file("sabot2.Dsk", "f")),
                file("river.wav", "1"),
                file("river.tap", "2"),
                file("ARTIL.COD", "3"),
                file("artil.asc", "4"),
                file("calc.foc", "5"),
                file("readme.txt", "6"));

        assertThat(ProgramFiles.of(BK, root))
                .containsExactlyInAnyOrder(
                        hash("a"), hash("b"), hash("c"), hash("d"), hash("e"), hash("f"), hash("1"), hash("2"),
                        hash("3"), hash("4"), hash("5"));
    }

    @Test
    void neverAreTheFilesWithoutAnExtensionOfBk() {
        ReleaseFile root = zip(
                "solit.zip", file("SOLIT.BIN", "a"), file("SOLIT-A", "b"), file("LICENSE", "c"), file("Makefile", "d"));

        assertThat(ProgramFiles.of(BK, root)).containsExactly(hash("a"));
    }

    @Test
    void isNothingOnAPlatformThatHasNoProgramFilesYet() {
        assertThat(ProgramFiles.of(new PlatformCode("orion"), zip("bundle.zip", file("ELITE.ROM", "a"))))
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
