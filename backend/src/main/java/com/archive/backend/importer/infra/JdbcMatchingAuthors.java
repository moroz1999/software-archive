package com.archive.backend.importer.infra;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.author.domain.RealName;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.importer.domain.source.MatchingAuthors;
import com.archive.backend.label.domain.LabelName;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcMatchingAuthors implements MatchingAuthors {

    /** A name by its letters and digits alone, whatever its case: "E.B.O. Soft" is "ebosoft". */
    private static final String KEY = "lower(regexp_replace(%s, '[^[:alnum:]]', '', 'g'))";

    private final JdbcClient jdbc;

    @Override
    public List<AuthorId> people(RealName realName, LabelName nickname) {
        return jdbc
                .sql("""
                        SELECT DISTINCT p.id FROM person p JOIN label l ON l.person_id = p.id
                        WHERE lower(trim(p.real_name)) = lower(trim(:realName)) AND lower(l.name) = lower(:nickname)
                        """)
                .param("realName", realName.value())
                .param("nickname", nickname.value())
                .query(UUID.class)
                .list()
                .stream()
                .map(AuthorId::new)
                .toList();
    }

    @Override
    public List<GroupId> groups(LabelName name) {
        // A name of no letter or digit is the same as any other such name: it tells nothing.
        if (name.value().codePoints().noneMatch(Character::isLetterOrDigit)) {
            return List.of();
        }
        return jdbc
                .sql("SELECT g.id FROM \"group\" g WHERE " + KEY.formatted("g.name") + " = " + KEY.formatted(":name")
                        + " UNION SELECT l.group_id FROM label l WHERE l.group_id IS NOT NULL AND "
                        + KEY.formatted("l.name") + " = " + KEY.formatted(":name"))
                .param("name", name.value())
                .query(UUID.class)
                .list()
                .stream()
                .map(GroupId::new)
                .toList();
    }
}
