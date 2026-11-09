package com.archive.backend.author.web.mapper;

import com.archive.backend.author.app.ListAuthorsQuery;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorDetails;
import com.archive.backend.author.web.dto.AuthorCatalogEntryDto;
import com.archive.backend.author.web.dto.AuthorDetailsDto;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.label.domain.LabelLetter;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface AuthorWebMapper {

    AuthorCatalogEntryDto toCatalogEntry(AuthorCatalogEntry entry);

    AuthorDetailsDto toDetails(AuthorDetails details);

    // Request scalars -> value objects.

    default ListAuthorsQuery toListQuery(
            @Nullable String letter,
            @Nullable UUID country,
            @Nullable UUID city,
            @Nullable String platform,
            Pageable pageable) {
        return new ListAuthorsQuery(
                letter == null ? null : new LabelLetter(letter),
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                platform == null ? null : new PlatformCode(platform),
                pageable);
    }
}
