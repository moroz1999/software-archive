package com.archive.backend.software.domain;

import java.util.Optional;

/**
 * Software as a domain entity, for the use cases that change it. Reads go through the read model.
 */
public interface SoftwareRepository {

    Optional<Software> findById(SoftwareId id);

    void save(Software software);

    void delete(Software software);
}
