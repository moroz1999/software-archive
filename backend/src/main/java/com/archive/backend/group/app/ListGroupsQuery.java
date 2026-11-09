package com.archive.backend.group.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.label.domain.LabelLetter;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

public record ListGroupsQuery(
        @Nullable LabelLetter letter,
        @Nullable CountryId country,
        @Nullable CityId city,
        Pageable pageable) {}
