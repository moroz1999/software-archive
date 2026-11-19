package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;

/** Builds the tree rooted at one uploaded release file. */
public interface ReleaseFileStructureParser {

    ReleaseFile parse(PlatformCode platform, ReleaseArchiveUpload upload);
}
