package com.archive.backend.assistant.web.entry;

import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareRelease;
import com.archive.backend.software.read.SoftwareScreenshot;
import java.util.List;

/** Software as its page shows it: the software, its own screenshots and its releases. */
public record SoftwareEntry(
        SoftwareDetails software, List<SoftwareScreenshot> screenshots, List<SoftwareRelease> releases) {}
