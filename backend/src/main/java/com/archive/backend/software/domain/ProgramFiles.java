package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The files that make a release of a platform, by which an import knows a program it has met before
 * (docs/domain/imports.md#software): its executable files, its floppy images and tape recordings, and on BK the
 * programs of BASIC and FOCAL too. A file without an extension is never one.
 */
public final class ProgramFiles {

    private static final Map<String, Set<String>> MEDIA = Map.of(
            "vector", Set.of("fdd", "cas", "wav"),
            "bk", Set.of("bkd", "img", "dsk", "wav", "tap", "cod", "asc", "foc"));

    private ProgramFiles() {}

    /** The digests of the program files in a parsed tree; a program file is not looked into. */
    public static Set<ReleaseFileHash> of(PlatformCode platform, ReleaseFile root) {
        Set<String> extensions = new HashSet<>(ExecutableFiles.extensionsOf(platform));
        extensions.addAll(MEDIA.getOrDefault(platform.value(), Set.of()));
        Set<ReleaseFileHash> found = new HashSet<>();
        collect(extensions, root, found);
        return Set.copyOf(found);
    }

    private static void collect(Set<String> extensions, ReleaseFile file, Set<ReleaseFileHash> found) {
        ReleaseFileHash md5 = file.md5();
        if (!file.directory() && md5 != null && extensions.contains(file.name().extension())) {
            found.add(md5);
            return;
        }
        file.children().forEach(child -> collect(extensions, child, found));
    }
}
