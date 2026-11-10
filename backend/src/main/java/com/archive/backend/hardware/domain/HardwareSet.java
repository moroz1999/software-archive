package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/**
 * Hardware chosen for a platform, always checked against the reference book: only {@link HardwareSets} builds a new
 * one, so a set that exists is valid (docs/domain/hardware.md).
 */
@ValueObject
public final class HardwareSet {

    private final PlatformCode platform;
    private final Set<HardwareCode> codes;

    HardwareSet(PlatformCode platform, Set<HardwareCode> codes) {
        this.platform = Objects.requireNonNull(platform, "platform");
        this.codes = Set.copyOf(codes);
    }

    /**
     * A set read back from storage, checked when it was stored; anything else goes through {@link HardwareSets}.
     */
    public static HardwareSet restore(PlatformCode platform, Set<HardwareCode> codes) {
        return new HardwareSet(platform, codes);
    }

    public PlatformCode platform() {
        return platform;
    }

    public Set<HardwareCode> codes() {
        return codes;
    }

    public boolean isEmpty() {
        return codes.isEmpty();
    }

    /**
     * No hardware fits any platform; any other set only its own.
     */
    public boolean belongsTo(PlatformCode platform) {
        return codes.isEmpty() || this.platform.equals(platform);
    }

    @Override
    public boolean equals(@Nullable Object other) {
        return other instanceof HardwareSet set && platform.equals(set.platform) && codes.equals(set.codes);
    }

    @Override
    public int hashCode() {
        return Objects.hash(platform, codes);
    }

    @Override
    public String toString() {
        return "HardwareSet[" + platform.value() + ", " + codes + "]";
    }
}
