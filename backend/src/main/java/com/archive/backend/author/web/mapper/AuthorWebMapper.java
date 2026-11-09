package com.archive.backend.author.web.mapper;

import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.web.dto.AuthorCatalogEntryDto;
import com.archive.backend.author.web.dto.AuthorDetailsDto;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

import java.util.UUID;

@Mapper(componentModel = "spring")
public interface AuthorWebMapper {

    AuthorCatalogEntryDto toCatalogEntry(AuthorCatalogEntry entry);

    AuthorDetailsDto toDetails(AuthorDetails details);

    // Request scalars -> value objects.

    default ListAuthorsQuery toListQuery(@Nullable String letter, @Nullable UUID country, @Nullable UUID city, Pageable pageable) {
        return new ListAuthorsQuery(
                letter == null ? null : new LabelLetter(letter),
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                pageable);
    }
}
