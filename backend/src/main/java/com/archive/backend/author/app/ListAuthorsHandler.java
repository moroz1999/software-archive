package com.archive.backend.author.app;

import com.archive.backend.label.domain.LabelLetter;
import com.archive.backend.author.read.AuthorCatalogEntry;
import com.archive.backend.author.read.AuthorReadRepository;
import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CountryId;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListAuthorsHandler {

    private final AuthorReadRepository reads;

    @Transactional(readOnly = true)
    public Page<AuthorCatalogEntry> handle(ListAuthorsQuery q) {
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
