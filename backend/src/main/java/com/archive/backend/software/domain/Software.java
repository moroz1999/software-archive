package com.archive.backend.software.domain;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.jspecify.annotations.Nullable;

import java.time.LocalDate;

@Getter
@AllArgsConstructor
public class Software {
    private final SoftwareId id;
    private SoftwareTitle title;
    private @Nullable SoftwareDescription description;
    private @Nullable LocalDate releaseDate;

    public static Software create(SoftwareId id, SoftwareTitle title, @Nullable SoftwareDescription description, @Nullable LocalDate releaseDate) {
        return new Software(id, title, description, releaseDate);
    }

    public void edit(SoftwareTitle title, @Nullable SoftwareDescription description, @Nullable LocalDate releaseDate) {
        this.title = title;
        this.description = description;
        this.releaseDate = releaseDate;
    }
}
