package com.archive.backend.group.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.group.read.GroupCatalogEntry;
import com.archive.backend.group.read.GroupReadRepository;
import com.archive.backend.label.domain.LabelLetter;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListGroupsHandler {

    private final GroupReadRepository reads;

    @Transactional(readOnly = true)
    public Page<GroupCatalogEntry> handle(ListGroupsQuery q) {
        LabelLetter letter = q.letter();
        CountryId country = q.country();
        CityId city = q.city();
        return reads.findCatalogPage(
                letter == null ? null : letter.value(),
                country == null ? null : country.value(),
                city == null ? null : city.value(),
                q.pageable());
    }
}
