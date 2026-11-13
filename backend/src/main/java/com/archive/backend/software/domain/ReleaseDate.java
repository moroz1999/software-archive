package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.time.LocalDate;
import java.time.Year;
import java.time.format.DateTimeParseException;
import java.util.Objects;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * When software or a release came out: a year, or a day of that year when the day is known (docs/domain/software.md).
 * It is written as {@code 1987} or {@code 1987-05-12}.
 */
@ValueObject
public record ReleaseDate(Year year, @Nullable LocalDate day) {

    private static final Pattern WRITTEN = Pattern.compile("\\d{4}(-\\d{2}-\\d{2})?");

    public ReleaseDate {
        Objects.requireNonNull(year, "year");
        if (day != null && day.getYear() != year.getValue()) {
            throw invalid();
        }
    }

    public static ReleaseDate ofYear(Year year) {
        return new ReleaseDate(year, null);
    }

    public static ReleaseDate ofDay(LocalDate day) {
        return new ReleaseDate(Year.of(day.getYear()), day);
    }

    public static ReleaseDate parse(String value) {
        if (!WRITTEN.matcher(value).matches()) {
            throw invalid();
        }
        if (value.length() == 4) {
            return ofYear(Year.parse(value));
        }
        try {
            return ofDay(LocalDate.parse(value));
        } catch (DateTimeParseException e) {
            throw invalid();
        }
    }

    private static InvalidValueException invalid() {
        return new InvalidValueException("release-date.invalid", "A release date is a year or a day of it.");
    }

    /**
     * As it is written: the day when it is known, otherwise the year.
     */
    public String iso() {
        return day == null ? year.toString() : day.toString();
    }
}
