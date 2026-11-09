package com.archive.backend.geo.app;

import com.archive.backend.geo.read.CountryItem;
import com.archive.backend.geo.read.GeoReadRepository;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ListCountriesHandler {

    private final GeoReadRepository reads;

    @Transactional(readOnly = true)
    public List<CountryItem> handle(ListCountriesQuery q) {
        return reads.findCountries();
    }
}
