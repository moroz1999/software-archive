package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.user.read.CurrentUser;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcUserReadRepository.class})
class JdbcUserReadRepositoryIT {

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AXE = UUID.fromString("00000000-0000-0000-0000-000000000002");

    @Autowired
    private JdbcUserReadRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insertUsers() {
        jdbc.update("""
                INSERT INTO app_user (id, name, email, password_hash, registered_at, status)
                VALUES (?, 'Zed', 'zed@example.com', 'hash', now(), 'CONFIRMED'),
                       (?, 'Axe', 'axe@example.com', 'hash', now(), 'UNCONFIRMED')""", ZED, AXE);
        jdbc.update("INSERT INTO app_user_group (user_id, user_group) VALUES (?, 'member'), (?, 'admin')", ZED, ZED);
    }

    @Test
    void tellsAConfirmedUserWithTheirGroups() {
        assertThat(repository.findCurrent(ZED))
                .contains(new CurrentUser(ZED, "Zed", "zed@example.com", List.of("admin", "member")));
    }

    @Test
    void anUnconfirmedAccountIsNobody() {
        assertThat(repository.findCurrent(AXE)).isEmpty();
    }
}
