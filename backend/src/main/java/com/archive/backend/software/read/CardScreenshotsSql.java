package com.archive.backend.software.read;

/**
 * The screenshots the card of software shows, as one SQL array of their ids (docs/domain/software.md#screenshots): its
 * own in their order; when it has none, those of its releases in the order of the releases; when it has none of these
 * either, the first two of the card of each program it collects, in the order of its programs. Never its maps, covers or
 * adverts. Shared by every read of cards, so the latest list, the catalog and the search show the same pictures.
 */
public final class CardScreenshotsSql {

    /** How many pictures of the card of each of its programs a compilation shows. */
    private static final int PER_PROGRAM = 2;

    private CardScreenshotsSql() {}

    /** The array expression for the software of the alias in the query around it. */
    public static String of(String software) {
        return "COALESCE(%s, %s, ARRAY[]::uuid[])".formatted(ownOrOfReleases(software), ofPrograms(software));
    }

    /** Its own screenshots, else those of its releases; NULL without any. */
    private static String ownOrOfReleases(String software) {
        return """
                COALESCE(
                    (SELECT array_agg(own.id ORDER BY own.position)
                     FROM software_screenshot own WHERE own.software_id = %1$s.id AND own.gallery = 'screenshots'),
                    (SELECT array_agg(shot.id ORDER BY %2$s, lower(COALESCE(r.title, %1$s.title)),
                                                       r.id, shot.position)
                     FROM software_release r
                     JOIN software_release_screenshot shot ON shot.release_id = r.id
                     WHERE r.software_id = %1$s.id))""".formatted(software, ReleaseDateSql.order("r"));
    }

    /**
     * The first pictures of the card of each program of the compilation; NULL without any. A program without pictures
     * of its own or of its releases is a compilation showing those of its programs in turn, so the programs are walked
     * down the tree, each branch in the order of its programs, until pictures are found.
     */
    private static String ofPrograms(String software) {
        return """
                (WITH RECURSIVE program (top, path, id, pictures) AS (
                    SELECT item.n, ARRAY[]::bigint[], item.id, item.pictures
                    FROM (%1$s) item
                    UNION ALL
                    SELECT program.top, program.path || item.n, item.id, item.pictures
                    FROM program CROSS JOIN LATERAL (%2$s) item
                    WHERE program.pictures IS NULL)
                 SELECT array_agg(picture.id ORDER BY picture.top, picture.rank)
                 FROM (SELECT program.top, shown.id,
                              row_number() OVER (PARTITION BY program.top ORDER BY program.path, shown.n) AS rank
                       FROM program CROSS JOIN LATERAL unnest(program.pictures) WITH ORDINALITY AS shown (id, n)) picture
                 WHERE picture.rank <= %3$d)""".formatted(programsOf(software + ".id"), programsOf("program.id"), PER_PROGRAM);
    }

    /** The programs of the compilation of the id, numbered in their order, with their pictures of their own. */
    private static String programsOf(String compilation) {
        return """
                SELECT i.item_id AS id,
                       row_number() OVER (ORDER BY lower(p.title), p.id) AS n,
                       %s AS pictures
                FROM software_compilation_item i JOIN software p ON p.id = i.item_id
                WHERE i.compilation_id = %s""".formatted(ownOrOfReleases("p"), compilation);
    }
}
