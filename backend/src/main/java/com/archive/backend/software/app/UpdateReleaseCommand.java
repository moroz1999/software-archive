package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.software.domain.ReleaseArchiveRemoval;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseContent;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record UpdateReleaseCommand(
        Actor actor,
        SoftwareId softwareId,
        ReleaseId releaseId,
        ReleaseContent content,
        Set<HardwareCode> hardware,
        @Nullable ReleaseArchiveUpload archive,
        ReleaseArchiveRemoval removeArchive,
        List<SoftwareCreditDraft> credits) {

    public UpdateReleaseCommand(
            Actor actor,
            SoftwareId softwareId,
            ReleaseId releaseId,
            ReleaseContent content,
            Set<HardwareCode> hardware,
            @Nullable ReleaseArchiveUpload archive,
            ReleaseArchiveRemoval removeArchive) {
        this(actor, softwareId, releaseId, content, hardware, archive, removeArchive, List.of());
    }

    public UpdateReleaseCommand(
            Actor actor,
            SoftwareId softwareId,
            ReleaseId releaseId,
            ReleaseContent content,
            Set<HardwareCode> hardware) {
        this(actor, softwareId, releaseId, content, hardware, null, new ReleaseArchiveRemoval(false));
    }
}
