package com.archive.backend.group.web.mapper;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.group.app.ListGroupsQuery;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupDetails;
import com.archive.backend.group.web.dto.GroupCatalogEntryDto;
import com.archive.backend.group.web.dto.GroupDetailsDto;
import com.archive.backend.label.domain.LabelLetter;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.mapstruct.Mapper;
import org.springframework.data.domain.Pageable;

@Mapper(componentModel = "spring")
public interface GroupWebMapper {

    GroupCatalogEntryDto toCatalogEntry(GroupCatalogEntry entry);

    GroupDetailsDto toDetails(GroupDetails details);

    // Request scalars -> value objects.

    default ListGroupsQuery toListQuery(
            @Nullable String letter, @Nullable UUID country, @Nullable UUID city, Pageable pageable) {
        return new ListGroupsQuery(
                letter == null ? null : new LabelLetter(letter),
                country == null ? null : new CountryId(country),
                city == null ? null : new CityId(city),
                pageable);
    }
}
