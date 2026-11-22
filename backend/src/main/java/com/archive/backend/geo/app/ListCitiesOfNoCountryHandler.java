package com.archive.backend.geo.app;

import com.archive.backend.geo.read.GeoReadRepository;
import com.archive.backend.geo.read.GeographyCity;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListCitiesOfNoCountryHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public List<GeographyCity> handle(ListCitiesOfNoCountryQuery q) {
        return reads.findCitiesOfNoCountry(q.language().code());
    }
}
