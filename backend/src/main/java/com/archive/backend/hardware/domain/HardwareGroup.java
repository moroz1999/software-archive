package com.archive.backend.hardware.domain;

import com.archive.backend.shared.domain.ddd.AggregateRoot;
import com.archive.backend.user.domain.Actor;
import java.util.Objects;
import lombok.Getter;

/** A group hardware is shown under, kept by administrators (docs/domain/hardware.md#hardware). */
@Getter
@AggregateRoot
public final class HardwareGroup {

    private final HardwareGroupCode code;
    private HardwareNames names;

    public HardwareGroup(HardwareGroupCode code, HardwareNames names) {
        this.code = Objects.requireNonNull(code, "code");
        this.names = Objects.requireNonNull(names, "names");
    }

    /** The reference book of hardware is kept by administrators only (docs/domain/users.md). */
    public static void requireKeptBy(Actor actor) {
        actor.requireAllowed(actor.mayKeepHardware());
    }

    public void rename(HardwareNames names) {
        this.names = Objects.requireNonNull(names, "names");
    }

    /** Only a group with no hardware goes: nothing of the archive is lost with it. */
    public void requireDeletable(HardwareUses uses) {
        if (uses.groupInUse(code)) {
            throw new HardwareGroupInUseException();
        }
    }
}
