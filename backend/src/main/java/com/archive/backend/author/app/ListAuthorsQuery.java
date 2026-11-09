package com.archive.backend.author.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelLetter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

public record ListAuthorsQuery(
        @Nullable LabelLetter letter,
        @Nullable CountryId country,
        @Nullable CityId city,
        @Nullable PlatformCode platform,
        Pageable pageable) {}
