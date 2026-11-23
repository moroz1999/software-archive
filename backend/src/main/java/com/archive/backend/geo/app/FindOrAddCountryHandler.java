package com.archive.backend.geo.app;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.GeoNameRepository;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * The country of the archive of this name in any language; one it does not have is added with the name in the
 * language given (docs/domain/imports.md#authors).
 */
@Service
@RequiredArgsConstructor
public class FindOrAddCountryHandler {

    private final GeoNameRepository names;

    @Transactional
    public CountryId handle(FindOrAddCountryCommand c) {
        return names.countryNamed(c.name())
                .orElseGet(() -> names.addCountry(new CountryId(UUID.randomUUID()), c.language(), c.name()));
    }
}
