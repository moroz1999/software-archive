package com.archive.backend.software.domain;

import java.util.List;

/** Release archive files following the database transaction that stores or discards their metadata. */
public interface ReleaseArchiveRepository {

    void store(ReleaseArchiveId id, ReleaseArchiveUpload upload);

    void discard(List<ReleaseArchiveId> ids);
}
