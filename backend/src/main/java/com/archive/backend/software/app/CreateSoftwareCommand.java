package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record CreateSoftwareCommand(
        Actor actor,
        SoftwareTitle title,
        @Nullable SoftwareDescription description,
        @Nullable ReleaseDate releaseDate,
        PlatformCode platform,
        Set<HardwareCode> hardware,
        Set<CategoryId> categories,
        Set<SoftwareReference> publishers,
        Set<SoftwareReference> developers,
        List<SoftwareCreditDraft> credits) {

    public CreateSoftwareCommand(
            Actor actor,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories,
            Set<SoftwareReference> publishers,
            Set<SoftwareReference> developers) {
        this(actor, title, description, releaseDate, platform, hardware, categories, publishers, developers, List.of());
    }

    public CreateSoftwareCommand(
            Actor actor,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable ReleaseDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories) {
        this(actor, title, description, releaseDate, platform, hardware, categories, Set.of(), Set.of(), List.of());
    }
}
