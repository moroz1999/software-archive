package com.archive.backend.importer.domain.bkcatalog;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.software.domain.ReleaseDate;
import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.Year;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jspecify.annotations.Nullable;

/**
 * The dates of bk-catalog as its entries write them (docs/domain/imports.md#bk-catalog): a day in one of three
 * forms, or a year, alone or with its month, which the archive keeps as the year. Several years tell no one date.
 */
final class BkCatalogDates {

    private static final Pattern DAY = Pattern.compile("(\\d{1,2})\\.(\\d{1,2})\\.(\\d{4}|\\d{2})");
    private static final Pattern DAY_YEAR_FIRST = Pattern.compile("(\\d{4})\\.(\\d{1,2})\\.(\\d{1,2})");
    private static final Pattern YEAR = Pattern.compile("(?:([A-Za-z]+)\\s+)?(\\d{4})");
    private static final Pattern YEARS = Pattern.compile("\\d{4}(?:\\s*[,-]\\s*\\d{4})+");
    private static final Set<String> MONTHS = Set.of(
            "january",
            "february",
            "march",
            "april",
            "may",
            "june",
            "july",
            "august",
            "september",
            "october",
            "november",
            "december");
    /** A two-digit year from this one on is of the 1900s, any before it of the 2000s. */
    private static final int LAST_CENTURY_FROM = 80;

    private BkCatalogDates() {}

    /** A date as it was read: none for nothing written or several years, and whether what is written was read. */
    @ValueObject
    record Reading(@Nullable ReleaseDate date, boolean readable) {}

    static Reading read(String written) {
        if (written.isEmpty() || YEARS.matcher(written).matches()) {
            return new Reading(null, true);
        }
        try {
            Matcher day = DAY.matcher(written);
            if (day.matches()) {
                return day(year(day.group(3)), day.group(2), day.group(1));
            }
            Matcher yearFirst = DAY_YEAR_FIRST.matcher(written);
            if (yearFirst.matches()) {
                return day(Integer.parseInt(yearFirst.group(1)), yearFirst.group(2), yearFirst.group(3));
            }
        } catch (DateTimeException e) {
            return new Reading(null, false);
        }
        Matcher year = YEAR.matcher(written);
        if (year.matches()
                && (year.group(1) == null || MONTHS.contains(year.group(1).toLowerCase(Locale.ROOT)))) {
            return new Reading(ReleaseDate.ofYear(Year.parse(year.group(2))), true);
        }
        return new Reading(null, false);
    }

    /** The year of a date, for software dated by its year; none without a date. */
    static @Nullable ReleaseDate yearOf(@Nullable ReleaseDate date) {
        return date == null ? null : ReleaseDate.ofYear(date.year());
    }

    private static Reading day(int year, String month, String day) {
        return new Reading(ReleaseDate.ofDay(LocalDate.of(year, Integer.parseInt(month), Integer.parseInt(day))), true);
    }

    private static int year(String written) {
        int year = Integer.parseInt(written);
        if (written.length() == 4) {
            return year;
        }
        return year >= LAST_CENTURY_FROM ? 1900 + year : 2000 + year;
    }
}
