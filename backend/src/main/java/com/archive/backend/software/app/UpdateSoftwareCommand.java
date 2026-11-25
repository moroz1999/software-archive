package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.PreservationStatus;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareInstructions;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record UpdateSoftwareCommand(
        Actor actor,
        SoftwareId id,
        SoftwareTitle title,
        @Nullable SoftwareDescription description,
        @Nullable ReleaseDate releaseDate,
        PlatformCode platform,
        Set<HardwareCode> hardware,
        Set<CategoryId> categories,
        List<SoftwareCreditDraft> credits,
        PreservationStatus preservation,
        @Nullable SoftwareInstructions instructions) {

    public UpdateSoftwareCommand(
            Actor actor,
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories,
            List<SoftwareCreditDraft> credits,
            PreservationStatus preservation) {
        this(actor, id, title, description, releaseDate, platform, hardware, categories, credits, preservation, null);
    }

    public UpdateSoftwareCommand(
            Actor actor,
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories) {
        this(
                actor,
                id,
                title,
                description,
                releaseDate,
                platform,
                hardware,
                categories,
                List.of(),
                PreservationStatus.UNKNOWN);
    }

    public UpdateSoftwareCommand(
            Actor actor,
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories,
            List<SoftwareCreditDraft> credits) {
        this(
                actor,
                id,
                title,
                description,
                releaseDate,
                platform,
                hardware,
                categories,
                credits,
                PreservationStatus.UNKNOWN);
    }
}
