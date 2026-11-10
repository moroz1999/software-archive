package com.archive.backend.hardware.read;

import java.util.List;

/**
 * Reads platforms as plain read models (docs/backend.md).
 */
public interface PlatformReadRepository {

    /**
     * All platforms by name.
     */
    List<PlatformItem> findAll();
}
