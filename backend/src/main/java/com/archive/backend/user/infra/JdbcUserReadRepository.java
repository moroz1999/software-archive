package com.archive.backend.user.infra;

import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserEntry;
import com.archive.backend.user.read.UserReadRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.jdbc.core.simple.JdbcClient;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcUserReadRepository implements UserReadRepository {

    private final JdbcClient jdbc;

    @Override
    public Optional<CurrentUser> findCurrent(UUID id) {
        List<String> groups = jdbc.sql("SELECT user_group FROM app_user_group WHERE user_id = :id ORDER BY user_group")
                .param("id", id)
                .query(String.class)
                .list();
        return jdbc.sql("SELECT id, name, email FROM app_user WHERE id = :id AND status = 'CONFIRMED'")
                .param("id", id)
                .query((row, n) -> new CurrentUser(
                        row.getObject("id", UUID.class), row.getString("name"), row.getString("email"), groups))
                .optional();
    }

    @Override
    public Page<UserEntry> findPage(@Nullable String search, Pageable pageable) {
        // strpos takes the search as text: % and _ are not wildcards.
        String where = search == null
                ? ""
                : "WHERE strpos(lower(u.name), lower(:search)) > 0 OR strpos(u.email, lower(:search)) > 0";
        long total = withSearch(jdbc.sql("SELECT count(*) FROM app_user u " + where), search)
                .query(Long.class)
                .single();
        List<UserEntry> entries = withSearch(jdbc.sql("""
                SELECT u.id, u.name, u.email, u.status, u.registered_at,
                       ARRAY(SELECT g.user_group FROM app_user_group g
                             WHERE g.user_id = u.id ORDER BY g.user_group) AS groups
                FROM app_user u
                """ + where + """

                ORDER BY lower(u.name), u.id
                LIMIT :limit OFFSET :offset"""), search)
                .param("limit", pageable.getPageSize())
                .param("offset", pageable.getOffset())
                .query((row, n) -> new UserEntry(
                        row.getObject("id", UUID.class),
                        row.getString("name"),
                        row.getString("email"),
                        "CONFIRMED".equals(row.getString("status")),
                        row.getTimestamp("registered_at").toInstant(),
                        List.of((String[]) row.getArray("groups").getArray())))
                .list();
        return new PageImpl<>(entries, pageable, total);
    }

    private static JdbcClient.StatementSpec withSearch(JdbcClient.StatementSpec statement, @Nullable String search) {
        return search == null ? statement : statement.param("search", search);
    }
}
