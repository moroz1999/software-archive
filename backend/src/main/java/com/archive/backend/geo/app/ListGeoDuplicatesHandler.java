package com.archive.backend.geo.app;

import com.archive.backend.geo.read.GeoDuplicates;
import com.archive.backend.geo.read.GeoReadRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListGeoDuplicatesHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public GeoDuplicates handle(ListGeoDuplicatesQuery q) {
        return reads.findDuplicates(q.language().code());
    }
}
