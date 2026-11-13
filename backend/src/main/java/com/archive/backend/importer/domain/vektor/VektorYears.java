package com.archive.backend.importer.domain.vektor;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.time.Year;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * The year of a program on vektor-06c.ru: of the software and of its one release. "1987 / 2026" is a work of 1987
 * released for the Vector in 2026; a single year is the year of both.
 */
@ValueObject
public record VektorYears(@Nullable Year software, @Nullable Year release) {

    private static final Pattern YEARS = Pattern.compile("\\s*(\\d{4})\\s*(?:[/-]\\s*(\\d{4})\\s*)?");

    public static VektorYears parse(String line) {
        Matcher years = YEARS.matcher(line);
        if (!years.matches()) {
            return new VektorYears(null, null);
        }
        Year software = Year.parse(years.group(1));
        String release = years.group(2);
        return new VektorYears(software, release == null ? software : Year.parse(release));
    }
}
