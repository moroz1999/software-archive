package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityNotFoundException;
import com.archive.backend.geo.read.CityDetails;
import com.archive.backend.geo.read.GeoReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class GetCityHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public CityDetails handle(GetCityQuery q) {
        return reads.findCity(q.id().value()).orElseThrow(CityNotFoundException::new);
    }
}
