package com.archive.backend.geo.app;

import com.archive.backend.geo.read.GeoReadRepository;
import com.archive.backend.geo.read.GeographyCountry;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListGeographyHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public List<GeographyCountry> handle(ListGeographyQuery q) {
        return reads.findGeography(q.language().code());
    }
}
