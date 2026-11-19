package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;
import java.util.List;
import java.util.Set;

/** The parsed trees of release files, looked up by the files in them. */
public interface ReleaseFileTrees {

    /** The trees of the releases of software of the platform that hold any file of these digests. */
    List<ReleasedFileTree> holdingAnyOf(PlatformCode platform, Set<ReleaseFileHash> files);
}
