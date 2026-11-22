package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CountryNotFoundException;
import com.archive.backend.geo.read.CountryDetails;
import com.archive.backend.geo.read.GeoReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCountryHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public CountryDetails handle(GetCountryQuery q) {
        return reads.findCountry(q.id().value()).orElseThrow(CountryNotFoundException::new);
    }
}
