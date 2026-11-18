package com.archive.backend.hardware.read;

import java.util.List;

/**
 * Reads platforms as plain read models (docs/backend.md).
 */
public interface PlatformReadRepository {

    /**
     * All platforms, each named in the language of the given code, or in Russian when it has no name in that one;
     * by that name.
     */
    List<PlatformItem> findAll(String language);
}
