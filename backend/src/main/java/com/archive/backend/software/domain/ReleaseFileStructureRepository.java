package com.archive.backend.software.domain;

/** The parsed tree derived from a release's downloadable file. */
public interface ReleaseFileStructureRepository {

    void replace(ReleaseId release, ReleaseFile root);

    void discard(ReleaseId release);
}
