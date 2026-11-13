package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.user.domain.AddedBy;
import java.util.Objects;

/**
 * A release of software; it lives only inside its software, which keeps its hardware on its own platform
 * (docs/domain/software.md). Its hardware replaces whole groups of its software (docs/domain/hardware.md).
 */
public final class Release {

    private final ReleaseId id;
    private ReleaseContent content;
    private HardwareSet hardware;
    private final AddedBy addedBy;

    public Release(ReleaseId id, ReleaseContent content, HardwareSet hardware, AddedBy addedBy) {
        this.id = Objects.requireNonNull(id, "id");
        this.content = Objects.requireNonNull(content, "content");
        this.hardware = Objects.requireNonNull(hardware, "hardware");
        this.addedBy = Objects.requireNonNull(addedBy, "addedBy");
    }

    public Release(ReleaseId id, ReleaseContent content, HardwareSet hardware) {
        this(id, content, hardware, AddedBy.NOBODY);
    }

    public ReleaseId id() {
        return id;
    }

    public ReleaseContent content() {
        return content;
    }

    public HardwareSet hardware() {
        return hardware;
    }

    public AddedBy addedBy() {
        return addedBy;
    }

    void edit(ReleaseContent content, HardwareSet hardware) {
        this.content = Objects.requireNonNull(content, "content");
        this.hardware = Objects.requireNonNull(hardware, "hardware");
    }
}
