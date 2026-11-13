package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.importer.domain.Importer;
import com.archive.backend.user.read.CurrentUser;
import com.archive.backend.user.read.UserEntry;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.jdbc.core.JdbcTemplate;

@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcUserReadRepository.class})
class JdbcUserReadRepositoryIT {

    private static final UUID ZED = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AXE = UUID.fromString("00000000-0000-0000-0000-000000000002");
    private static final UUID BOB = UUID.fromString("00000000-0000-0000-0000-000000000003");
    private static final Instant REGISTERED = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private JdbcUserReadRepository repository;

    @Autowired
    private JdbcTemplate jdbc;

    @BeforeEach
    void insertUsers() {
        // The importer's account the schema brings is not one of the accounts these lists are about.
        jdbc.update("DELETE FROM app_user WHERE id = ?", Importer.ID.value());
        jdbc.update("""
                INSERT INTO app_user (id, name, email, password_hash, registered_at, status)
                VALUES (?, 'Zed', 'zed@example.com', 'hash', ?, 'CONFIRMED'),
                       (?, 'Axe', 'axe@example.com', 'hash', ?, 'UNCONFIRMED')""", ZED, Timestamp.from(REGISTERED), AXE, Timestamp.from(REGISTERED));
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

    @Test
    void listsEveryAccountByItsNameWhateverTheCaseWithItsGroups() {
        insertBob();

        assertThat(repository.findPage(null, PageRequest.of(0, 50)).getContent())
                .containsExactly(
                        new UserEntry(AXE, "Axe", "axe@example.com", false, REGISTERED, List.of()),
                        new UserEntry(BOB, "bob", "bob@mail.test", true, REGISTERED, List.of("member", "moderator")),
                        new UserEntry(ZED, "Zed", "zed@example.com", true, REGISTERED, List.of("admin", "member")));
    }

    @Test
    void listsTheAccountsWhoseNameOrEmailHoldsTheSearchWhateverTheCase() {
        insertBob();

        assertThat(repository.findPage("EXAMPLE", PageRequest.of(0, 50)).getContent())
                .extracting(UserEntry::name)
                .containsExactly("Axe", "Zed");
        assertThat(repository.findPage("ob", PageRequest.of(0, 50)).getContent())
                .extracting(UserEntry::name)
                .containsExactly("bob");
    }

    @Test
    void takesASearchForTextNotForAPattern() {
        assertThat(repository.findPage("%", PageRequest.of(0, 50)).getContent()).isEmpty();
        assertThat(repository.findPage("_ed", PageRequest.of(0, 50)).getContent())
                .isEmpty();
    }

    @Test
    void listsTheAccountsPageByPage() {
        insertBob();

        Page<UserEntry> second = repository.findPage(null, PageRequest.of(1, 2));

        assertThat(second.getContent()).extracting(UserEntry::name).containsExactly("Zed");
        assertThat(second.getTotalElements()).isEqualTo(3);
        assertThat(second.getTotalPages()).isEqualTo(2);
    }

    private void insertBob() {
        jdbc.update("""
                INSERT INTO app_user (id, name, email, password_hash, registered_at, status)
                VALUES (?, 'bob', 'bob@mail.test', 'hash', ?, 'CONFIRMED')""", BOB, Timestamp.from(REGISTERED));
        jdbc.update(
                "INSERT INTO app_user_group (user_id, user_group) VALUES (?, 'moderator'), (?, 'member')", BOB, BOB);
    }
}
