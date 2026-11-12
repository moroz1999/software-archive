package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record UpdateSoftwareCommand(
        SoftwareId id,
        SoftwareTitle title,
        @Nullable SoftwareDescription description,
        @Nullable LocalDate releaseDate,
        PlatformCode platform,
        Set<HardwareCode> hardware,
        Set<CategoryId> categories,
        Set<SoftwareReference> publishers,
        Set<SoftwareReference> developers,
        List<SoftwareCreditDraft> credits) {

    public UpdateSoftwareCommand(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories,
            Set<SoftwareReference> publishers,
            Set<SoftwareReference> developers) {
        this(id, title, description, releaseDate, platform, hardware, categories, publishers, developers, List.of());
    }

    public UpdateSoftwareCommand(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            PlatformCode platform,
            Set<HardwareCode> hardware,
            Set<CategoryId> categories) {
        this(id, title, description, releaseDate, platform, hardware, categories, Set.of(), Set.of(), List.of());
    }
}
