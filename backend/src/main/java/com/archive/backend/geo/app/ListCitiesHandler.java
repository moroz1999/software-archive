package com.archive.backend.geo.app;

import com.archive.backend.geo.read.CityItem;
import com.archive.backend.geo.read.GeoReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ListCitiesHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public List<CityItem> handle(ListCitiesQuery q) {
        return reads.findCities(q.country().value());
    }
}
