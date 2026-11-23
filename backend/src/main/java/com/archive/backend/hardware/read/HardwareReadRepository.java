package com.archive.backend.hardware.read;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Reads the hardware reference book as plain read models (docs/backend.md). Hardware is named as platforms are and goes
 * by its group in the order of the groups, then by that name.
 */
public interface HardwareReadRepository {

    /** The hardware of the platform; none for an unknown one. */
    List<HardwareItem> findByPlatform(String platform, String language);

    /** The hardware of the codes; a code the reference book does not have is left out. */
    List<HardwareItem> findByCodes(Collection<String> codes, String language);

    /** Every group in its order, named as platforms are, each with its hardware by name. */
    List<HardwareGroupItem> findGroups(String language);

    Optional<HardwareGroupDetails> findGroup(String code);

    Optional<HardwareDetails> findHardware(String code);
}
