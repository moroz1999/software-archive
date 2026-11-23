package com.archive.backend.hardware.domain;

import java.util.Set;

/** What of the archive has hardware or its groups: what is in use is kept (docs/domain/hardware.md#hardware). */
public interface HardwareUses {

    /** Whether software or a release has the hardware. */
    boolean inUse(HardwareCode hardware);

    /** Whether software of any of the platforms has the hardware, on itself or on a release of it. */
    boolean usedOn(HardwareCode hardware, Set<PlatformCode> platforms);

    /** Whether the group has hardware. */
    boolean groupInUse(HardwareGroupCode group);
}
