package com.archive.backend.software.read;

/**
 * The release date of software or of a release in SQL: the day when it is known, otherwise the year
 * (docs/domain/software.md), and the order of release dates.
 */
public final class ReleaseDateSql {

    private ReleaseDateSql() {}

    /**
     * The date as it is written, {@code 1987} or {@code 1987-05-12}, of the table under {@code alias}.
     */
    public static String of(String alias) {
        return "COALESCE(%1$s.release_day::text, %1$s.release_year::text)".formatted(alias);
    }

    /**
     * Oldest first; within a year the ones known only by the year before the days; unknown dates last.
     */
    public static String order(String alias) {
        return "%1$s.release_year NULLS LAST, %1$s.release_day NULLS FIRST".formatted(alias);
    }
}
