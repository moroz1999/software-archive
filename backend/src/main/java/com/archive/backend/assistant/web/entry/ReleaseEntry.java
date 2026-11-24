package com.archive.backend.assistant.web.entry;

import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.ReleaseFiles;
import com.archive.backend.software.read.SoftwareScreenshot;
import java.util.List;
import org.jspecify.annotations.Nullable;

/** A release as its page shows it: the release, its screenshots and the file tree of its archive, if it has one. */
public record ReleaseEntry(
        ReleaseDetails release,
        List<SoftwareScreenshot> screenshots,
        @Nullable ReleaseFiles files) {}
