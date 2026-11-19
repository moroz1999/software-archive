package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

/**
 * The files that make a release of a platform, by which an import knows a program it has met before
 * (docs/domain/imports.md#software): on Vector, ROM files, floppy images and tape recordings.
 */
public final class ProgramFiles {

    private static final Map<String, Set<String>> EXTENSIONS = Map.of("vector", Set.of("rom", "fdd", "cas", "wav"));

    private ProgramFiles() {}

    /** The digests of the program files in a parsed tree; a program file is not looked into. */
    public static Set<ReleaseFileHash> of(PlatformCode platform, ReleaseFile root) {
        Set<String> extensions = EXTENSIONS.getOrDefault(platform.value(), Set.of());
        Set<ReleaseFileHash> found = new HashSet<>();
        collect(extensions, root, found);
        return Set.copyOf(found);
    }

    private static void collect(Set<String> extensions, ReleaseFile file, Set<ReleaseFileHash> found) {
        ReleaseFileHash md5 = file.md5();
        if (!file.directory() && md5 != null && extensions.contains(extensionOf(file.name()))) {
            found.add(md5);
            return;
        }
        file.children().forEach(child -> collect(extensions, child, found));
    }

    private static String extensionOf(ReleaseFileName name) {
        String value = name.value();
        int dot = value.lastIndexOf('.');
        return dot < 0 ? "" : value.substring(dot + 1).toLowerCase(Locale.ROOT);
    }
}
