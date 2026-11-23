package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.GeoNameRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The city of the archive of this name in any language; one it does not have is added with the name in the language
 * given, in no country: its country is not known (docs/domain/geo.md, docs/domain/imports.md#authors).
 */
@Service
@RequiredArgsConstructor
public class FindOrAddCityHandler {

    private final GeoNameRepository names;

    @Transactional
    public CityId handle(FindOrAddCityCommand c) {
        return names.cityNamed(c.name())
                .orElseGet(() -> names.addCity(new CityId(UUID.randomUUID()), null, c.language(), c.name()));
    }
}
