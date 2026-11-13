package com.archive.backend.software.read;

/**
 * The screenshots the card of software shows, as one SQL array of their ids: its own in their order, or when it has
 * none, those of its releases in the order of the releases (docs/domain/software.md#screenshots). Shared by every read
 * of cards, so the latest list and the catalog show the same pictures.
 */
public final class CardScreenshotsSql {

    private CardScreenshotsSql() {}

    /** The array expression for the software of the alias in the query around it. */
    public static String of(String software) {
        return """
                COALESCE(
                    (SELECT array_agg(own.id ORDER BY own.position)
                     FROM software_screenshot own WHERE own.software_id = %1$s.id),
                    (SELECT array_agg(shot.id ORDER BY %2$s, lower(COALESCE(r.title, %1$s.title)),
                                                       r.id, shot.position)
                     FROM software_release r
                     JOIN software_release_screenshot shot ON shot.release_id = r.id
                     WHERE r.software_id = %1$s.id),
                    ARRAY[]::uuid[])""".formatted(software, ReleaseDateSql.order("r"));
    }
}
