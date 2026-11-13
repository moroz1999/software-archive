package com.archive.backend.software.domain;

import java.util.List;

/**
 * Where the pictures of screenshots are kept, outside the database. Both calls follow the transaction they run in: a
 * picture stored by a change that rolls back is deleted, a discarded one is deleted only once the change commits.
 */
public interface ScreenshotPictureRepository {

    void store(ScreenshotId id, ScreenshotImage image);

    void discard(List<ScreenshotId> ids);
}
