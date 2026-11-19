package com.archive.backend.geo.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

/** A city whose country is not known belongs to the country "Unknown" (docs/domain/geo.md); the schema holds it. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import(TestcontainersConfiguration.class)
class UnknownCountryIT {

    @Autowired
    private JdbcTemplate jdbc;

    @Test
    void holdsTheCountryUnknownNamedInRussianAndInEnglish() {
        assertThat(jdbc.queryForList(
                        "SELECT language || ':' || name FROM country_name"
                                + " WHERE country_id = '00000000-0000-0000-0000-00000000c001' ORDER BY 1",
                        String.class))
                .containsExactly("en:Unknown", "ru:Неизвестно");
    }
}
