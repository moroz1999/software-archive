package com.archive.backend.software.app;

import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import java.time.LocalDate;
import java.util.Set;
import org.jspecify.annotations.Nullable;

public record UpdateSoftwareCommand(
        SoftwareId id,
        SoftwareTitle title,
        @Nullable SoftwareDescription description,
        @Nullable LocalDate releaseDate,
        PlatformCode platform,
        Set<HardwareCode> hardware) {}
