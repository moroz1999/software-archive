package com.archive.backend.label.infra;

import com.archive.backend.label.read.LabelDetails;
import com.archive.backend.label.read.LabelReadRepository;
import com.archive.backend.label.read.MainNameSql;
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
    public Optional<LabelDetails> findById(UUID id) {
        return jdbc.sql("SELECT name, person_id, group_id FROM label WHERE id = :id")
                .param("id", id)
                .query((rs, row) -> {
                    UUID person = rs.getObject("person_id", UUID.class);
                    UUID group = rs.getObject("group_id", UUID.class);
                    LabelDetails.Owner owner = person != null
                            ? new LabelDetails.Owner("author", person)
                            : group != null ? new LabelDetails.Owner("group", group) : null;
                    return new LabelDetails(id, rs.getString("name"), owner, groups(id), false);
                })
                .optional();
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
