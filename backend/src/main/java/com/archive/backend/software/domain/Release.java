package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.HardwareSet;
import java.util.Objects;

/**
 * A release of software; it lives only inside its software, which keeps its hardware on its own platform
 * (docs/domain/software.md). Its hardware replaces whole groups of its software (docs/domain/hardware.md).
 */
public final class Release {

    private final ReleaseId id;
    private ReleaseContent content;
    private HardwareSet hardware;

    public Release(ReleaseId id, ReleaseContent content, HardwareSet hardware) {
        this.id = Objects.requireNonNull(id, "id");
        this.content = Objects.requireNonNull(content, "content");
        this.hardware = Objects.requireNonNull(hardware, "hardware");
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

    void edit(ReleaseContent content, HardwareSet hardware) {
        this.content = Objects.requireNonNull(content, "content");
        this.hardware = Objects.requireNonNull(hardware, "hardware");
    }
}
