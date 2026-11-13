package com.archive.backend.importer.domain.vektor;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.Year;
import org.junit.jupiter.api.Test;

class VektorYearsTest {

    @Test
    void givesOneYearToTheSoftwareAndItsRelease() {
        assertThat(VektorYears.parse("1992")).isEqualTo(new VektorYears(Year.of(1992), Year.of(1992)));
        assertThat(VektorYears.parse(" 2009 ")).isEqualTo(new VektorYears(Year.of(2009), Year.of(2009)));
    }

    @Test
    void givesTheFirstOfTwoYearsToTheSoftwareAndTheSecondToTheRelease() {
        assertThat(VektorYears.parse("1987 / 2026")).isEqualTo(new VektorYears(Year.of(1987), Year.of(2026)));
        assertThat(VektorYears.parse("1994 - 2026")).isEqualTo(new VektorYears(Year.of(1994), Year.of(2026)));
    }

    @Test
    void knowsNoYearWhereTheSiteGivesNone() {
        VektorYears unknown = new VektorYears(null, null);
        assertThat(VektorYears.parse("-")).isEqualTo(unknown);
        assertThat(VektorYears.parse("")).isEqualTo(unknown);
        assertThat(VektorYears.parse("давно")).isEqualTo(unknown);
    }
}
