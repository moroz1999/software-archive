package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

@Getter
@AllArgsConstructor
public class Software {
    private final SoftwareId id;
    private SoftwareTitle title;
    private @Nullable SoftwareDescription description;
    private @Nullable LocalDate releaseDate;
    private PlatformCode platform;

    public static Software create(
            SoftwareId id,
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            PlatformCode platform) {
        return new Software(id, title, description, releaseDate, platform);
    }

    public void edit(
            SoftwareTitle title,
            @Nullable SoftwareDescription description,
            @Nullable LocalDate releaseDate,
            PlatformCode platform) {
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
        this.platform = platform;
    }
}
