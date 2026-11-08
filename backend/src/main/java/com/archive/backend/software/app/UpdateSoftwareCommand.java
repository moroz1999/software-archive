package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareDescription;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

public record UpdateSoftwareCommand(SoftwareId id, SoftwareTitle title, @Nullable SoftwareDescription description, @Nullable LocalDate releaseDate) {
}
