package com.archive.backend.software.app;

import com.archive.backend.category.domain.CategoryId;
import com.archive.backend.software.domain.ReleaseDate;
import com.archive.backend.software.domain.ScreenshotImage;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.user.domain.Actor;
import java.util.List;
import java.util.Set;
import org.jspecify.annotations.Nullable;

/** What an import found of software the archive has, to add what it lacks. */
public record SupplementSoftwareCommand(
        Actor actor,
        SoftwareId softwareId,
        @Nullable SoftwareDescription description,
        @Nullable ReleaseDate releaseDate,
        Set<CategoryId> categories,
        List<SoftwareCreditDraft> credits,
        List<ScreenshotImage> screenshots) {}
