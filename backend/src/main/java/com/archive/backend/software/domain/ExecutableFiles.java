package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;
import java.util.Map;
import java.util.Set;

/** Which file names each platform executes directly: fixed per platform, kept here rather than edited. */
public final class ExecutableFiles {

    private static final Map<String, Set<String>> EXTENSIONS = Map.of("vector", Set.of("rom"));

    private ExecutableFiles() {}

    public static boolean isExecutable(PlatformCode platform, ReleaseFileName fileName) {
        String extension = fileName.extension();
        return !extension.isEmpty()
                && EXTENSIONS.getOrDefault(platform.value(), Set.of()).contains(extension);
    }
}
