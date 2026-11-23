package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Where an item of hardware is in the reference book: its group and its platforms, always checked against it: only
 * {@link HardwarePlacements} builds a new one, so a placement that exists is valid.
 */
@ValueObject
public final class HardwarePlacement {

    private final HardwareGroupCode group;
    private final HardwarePlatforms platforms;

    HardwarePlacement(HardwareGroupCode group, HardwarePlatforms platforms) {
        this.group = Objects.requireNonNull(group, "group");
        this.platforms = Objects.requireNonNull(platforms, "platforms");
    }

    /** A placement read back from storage, checked when it was stored; anything else goes through the factory. */
    public static HardwarePlacement restore(HardwareGroupCode group, HardwarePlatforms platforms) {
        return new HardwarePlacement(group, platforms);
    }

    public HardwareGroupCode group() {
        return group;
    }

    public Set<PlatformCode> platforms() {
        return platforms.values();
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof HardwarePlacement placement
                && group.equals(placement.group)
                && platforms.equals(placement.platforms);
    }

    @Override
    public int hashCode() {
        return Objects.hash(group, platforms);
    }

    @Override
    public String toString() {
        return "HardwarePlacement[" + group.value() + ", " + platforms.values() + "]";
    }
}
