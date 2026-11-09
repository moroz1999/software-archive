package com.archive.backend.author.app;

import com.archive.backend.author.domain.AuthorLetter;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Pageable;

public record ListAuthorsQuery(@Nullable AuthorLetter letter, @Nullable CountryId country, @Nullable CityId city, Pageable pageable) {
}
