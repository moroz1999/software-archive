package com.archive.backend.hardware.domain;

import java.util.Optional;

/** Hardware as domain entities, for the use cases that change it. Reads go through the read model. */
public interface HardwareRepository {

    Optional<Hardware> findByCode(HardwareCode code);

    /** Adds new hardware; refuses a code the reference book has already. */
    void add(Hardware hardware);

    void save(Hardware hardware);

    void delete(Hardware hardware);
}
