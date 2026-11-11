package com.archive.backend.group.read;

import java.util.List;

/**
 * Reads the reference book of group types.
 */
@FunctionalInterface
public interface GroupTypeReadRepository {

    /**
     * The codes of all group types, by code.
     */
    List<String> findAll();
}
