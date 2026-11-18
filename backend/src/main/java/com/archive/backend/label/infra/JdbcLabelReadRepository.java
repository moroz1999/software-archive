package com.archive.backend.label.infra;

import com.archive.backend.geo.read.LocationsSql;
import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.read.LabelReadRepository;
import com.archive.backend.label.read.MainNameSql;
import com.archive.backend.user.read.AdditionSql;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

/** The page of a label straight from SQL. */
@Repository
@RequiredArgsConstructor
public class JdbcLabelReadRepository implements LabelReadRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<LabelDetails> findById(UUID id, String language) {
        return jdbc.sql("WITH " + MainNameSql.PEOPLE + ", " + MainNameSql.GROUPS
                        + """
                        SELECT l.name, l.person_id, l.group_id, l.added_by, %s,
                               COALESCE(p.real_name, p.main_name, g.main_name) AS owner_name,
                               CASE WHEN p.real_name IS NOT NULL AND p.main_label_id IS NOT NULL THEN p.main_name END
                                   AS owner_note,
                               COALESCE(person.added_by, owner_group.added_by) AS owner_added_by
                        FROM label l
                        LEFT JOIN person ON person.id = l.person_id
                        LEFT JOIN "group" owner_group ON owner_group.id = l.group_id
                        LEFT JOIN named_person p ON p.person_id = l.person_id
                        LEFT JOIN named_group g ON g.group_id = l.group_id
                        WHERE l.id = :id
                        """.formatted(AdditionSql.columns("l")))
                .param("id", id)
                .query((rs, row) -> {
                    UUID person = rs.getObject("person_id", UUID.class);
                    UUID group = rs.getObject("group_id", UUID.class);
                    String ownerName = rs.getString("owner_name");
                    UUID addedBy = rs.getObject("owner_added_by", UUID.class);
                    LabelDetails.Owner owner = person != null
                            ? new LabelDetails.Owner("author", person, ownerName, rs.getString("owner_note"), addedBy)
                            : group != null ? new LabelDetails.Owner("group", group, ownerName, null, addedBy) : null;
                    return new LabelDetails(
                            id,
                            rs.getString("name"),
                            owner,
                            groups(id),
                            locations(id, language),
                            origins(id),
                            AdditionSql.of(rs),
                            rs.getObject("added_by", UUID.class),
                            LabelDetails.Rights.NONE);
                })
                .optional();
    }

    private List<LabelDetails.Location> locations(UUID labelId, String language) {
        return jdbc.sql(LocationsSql.of("LABEL"))
                .param("id", labelId)
                .param("language", language)
                .query((rs, row) -> new LabelDetails.Location(
                        rs.getObject("country_id", UUID.class),
                        rs.getString("country"),
                        rs.getObject("city_id", UUID.class),
                        rs.getString("city"),
                        rs.getBoolean("is_primary")))
                .list();
    }

    /** The imports the label came from, by the source archive. */
    private List<LabelDetails.Origin> origins(UUID labelId) {
        return jdbc.sql("""
                        SELECT origin, external_id, url FROM external_origin
                        WHERE kind = 'LABEL' AND entity_id = :id
                        ORDER BY origin, external_id
                        """)
                .param("id", labelId)
                .query((rs, row) -> new LabelDetails.Origin(
                        rs.getString("origin"), rs.getString("external_id"), rs.getString("url")))
                .list();
    }

    /** The groups the label was a member of, under their main names. */
    private List<LabelDetails.MemberOf> groups(UUID labelId) {
        return jdbc.sql("WITH " + MainNameSql.GROUPS + """
                        SELECT g.group_id, g.main_name
                        FROM named_group g
                        WHERE EXISTS (SELECT 1 FROM group_member gm WHERE gm.group_id = g.group_id AND gm.label_id = :id)
                        ORDER BY lower(g.main_name), g.group_id
                        """)
                .param("id", labelId)
                .query((rs, row) ->
                        new LabelDetails.MemberOf(rs.getObject("group_id", UUID.class), rs.getString("main_name")))
                .list();
    }
}
