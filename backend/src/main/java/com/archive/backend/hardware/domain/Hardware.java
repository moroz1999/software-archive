package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.shared.domain.ddd.DomainEvent;
import com.archive.backend.user.domain.Actor;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import lombok.Getter;

/** An item of the hardware reference book, kept by administrators (docs/domain/hardware.md#hardware). */
@Getter
@AggregateRoot
public final class Hardware {

    private final HardwareCode code;
    private HardwareNames names;
    private HardwarePlacement placement;

    @Getter(lombok.AccessLevel.NONE)
    private final List<DomainEvent> events = new ArrayList<>();

    public Hardware(HardwareCode code, HardwareNames names, HardwarePlacement placement) {
        this.code = Objects.requireNonNull(code, "code");
        this.names = Objects.requireNonNull(names, "names");
        this.placement = Objects.requireNonNull(placement, "placement");
    }

    /** The reference book of hardware is kept by administrators only (docs/domain/users.md). */
    public static void requireKeptBy(Actor actor) {
        actor.requireAllowed(actor.mayKeepHardware());
    }

    /**
     * Gives the hardware other names, another group or other platforms. A platform software of which has the hardware
     * stays: that software would have hardware of another platform. Another group tells the software that has it.
     */
    public void edit(HardwareNames names, HardwarePlacement placement, HardwareUses uses) {
        Set<PlatformCode> removed = new HashSet<>(this.placement.platforms());
        removed.removeAll(placement.platforms());
        if (!removed.isEmpty() && uses.usedOn(code, removed)) {
            throw new HardwarePlatformInUseException();
        }
        if (!placement.group().equals(this.placement.group())) {
            events.add(new HardwareRegrouped(code));
        }
        this.names = Objects.requireNonNull(names, "names");
        this.placement = placement;
    }

    /** Only hardware no software and no release has goes: nothing of the archive is lost with it. */
    public void requireDeletable(HardwareUses uses) {
        if (uses.inUse(code)) {
            throw new HardwareInUseException();
        }
    }

    /** What happened to the hardware since it was loaded, handed over once. */
    public List<DomainEvent> pullEvents() {
        List<DomainEvent> pulled = List.copyOf(events);
        events.clear();
        return pulled;
    }
}
