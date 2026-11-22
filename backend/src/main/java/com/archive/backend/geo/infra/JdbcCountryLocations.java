package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CountryId;
import com.archive.backend.geo.domain.CountryLocations;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCountryLocations implements CountryLocations {

    private final JdbcClient jdbc;

    @Override
    public void mergeInto(CountryId country, CountryId into) {
        // A city keeps its places when it moves; only an owner in both countries without a city has one place twice.
        jdbc.sql("""
                        UPDATE entity_location kept SET is_primary = true
                        FROM entity_location gone
                        WHERE gone.country_id = :country AND gone.city_id IS NULL AND gone.is_primary
                          AND kept.country_id = :into AND kept.city_id IS NULL
                          AND kept.entity_type = gone.entity_type AND kept.entity_id = gone.entity_id
                        """)
                .param("country", country.value())
                .param("into", into.value())
                .update();
        jdbc.sql("""
                        DELETE FROM entity_location gone
                        USING entity_location kept
                        WHERE gone.country_id = :country AND gone.city_id IS NULL
                          AND kept.country_id = :into AND kept.city_id IS NULL
                          AND kept.entity_type = gone.entity_type AND kept.entity_id = gone.entity_id
                        """)
                .param("country", country.value())
                .param("into", into.value())
                .update();
        jdbc.sql("UPDATE entity_location SET country_id = :into WHERE country_id = :country")
                .param("into", into.value())
                .param("country", country.value())
                .update();
        jdbc.sql("UPDATE city SET country_id = :into WHERE country_id = :country")
                .param("into", into.value())
                .param("country", country.value())
                .update();
    }
}
