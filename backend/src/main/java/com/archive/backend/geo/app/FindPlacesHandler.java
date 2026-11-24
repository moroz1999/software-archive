package com.archive.backend.geo.app;

import com.archive.backend.geo.read.FoundPlace;
import com.archive.backend.geo.read.GeoReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** Countries and cities by a name of theirs in any language (docs/domain/geo.md). */
@Service
@RequiredArgsConstructor
public class FindPlacesHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public List<FoundPlace> handle(FindPlacesQuery q) {
        return reads.findPlaces(q.name().value(), q.language().code());
    }
}
