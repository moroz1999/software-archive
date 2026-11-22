package com.archive.backend.geo.infra;

import com.archive.backend.geo.domain.CityId;
import com.archive.backend.geo.domain.CityLocations;
import com.archive.backend.geo.domain.CountryId;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcCityLocations implements CityLocations {

    private final JdbcClient jdbc;

    @Override
    public void moveTo(CityId city, @Nullable CountryId country) {
        jdbc.sql("UPDATE entity_location SET country_id = :country WHERE city_id = :city")
                .param("country", country == null ? null : country.value())
                .param("city", city.value())
                .update();
    }

    @Override
    public void moveOutTo(CityId city, CountryId country) {
        jdbc.sql("UPDATE entity_location SET city_id = NULL, country_id = :country WHERE city_id = :city")
                .param("country", country.value())
                .param("city", city.value())
                .update();
    }

    @Override
    public void mergeInto(CityId city, CityId into, @Nullable CountryId country) {
        // An owner in both cities keeps the place of the one that stays, primary when either was.
        jdbc.sql("""
                        UPDATE entity_location kept SET is_primary = true
                        FROM entity_location gone
                        WHERE gone.city_id = :city AND gone.is_primary AND kept.city_id = :into
                          AND kept.entity_type = gone.entity_type AND kept.entity_id = gone.entity_id
                        """).param("city", city.value()).param("into", into.value()).update();
        jdbc.sql("""
                        DELETE FROM entity_location gone
                        USING entity_location kept
                        WHERE gone.city_id = :city AND kept.city_id = :into
                          AND kept.entity_type = gone.entity_type AND kept.entity_id = gone.entity_id
                        """).param("city", city.value()).param("into", into.value()).update();
        jdbc.sql("UPDATE entity_location SET city_id = :into, country_id = :country WHERE city_id = :city")
                .param("into", into.value())
                .param("country", country == null ? null : country.value())
                .param("city", city.value())
                .update();
        jdbc.sql("""
                        INSERT INTO label_city (label_id, city_id)
                        SELECT label_id, :into FROM label_city WHERE city_id = :city
                        ON CONFLICT DO NOTHING
                        """).param("into", into.value()).param("city", city.value()).update();
        jdbc.sql("DELETE FROM label_city WHERE city_id = :city")
                .param("city", city.value())
                .update();
    }
}
