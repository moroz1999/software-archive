package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record CreateReleaseCommand(
        Actor actor,
        SoftwareId softwareId,
        ReleaseContent content,
        Set<HardwareCode> hardware,
        @Nullable ReleaseArchiveUpload archive) {

    public CreateReleaseCommand(
            Actor actor, SoftwareId softwareId, ReleaseContent content, Set<HardwareCode> hardware) {
        this(actor, softwareId, content, hardware, null);
    }
}
