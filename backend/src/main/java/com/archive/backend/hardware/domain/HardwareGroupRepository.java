package com.archive.backend.hardware.domain;

import java.util.Optional;

/** Groups of hardware as domain entities, for the use cases that change them. Reads go through the read model. */
public interface HardwareGroupRepository {

    Optional<HardwareGroup> findByCode(HardwareGroupCode code);

    /** Adds a new group after the others; refuses a code another group has already. */
    void add(HardwareGroup group);

    void save(HardwareGroup group);

    void delete(HardwareGroup group);
}
