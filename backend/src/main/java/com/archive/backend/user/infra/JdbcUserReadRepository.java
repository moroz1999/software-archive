package com.archive.backend.user.infra;

import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserReadRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
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
}
