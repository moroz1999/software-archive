package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.IntegrationTest;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.GrantedGroups;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.PendingConfirmation;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserStatus;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.transaction.annotation.Transactional;

@IntegrationTest
@Transactional
class JpaUserRepositoryIT {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private JdbcTemplate jdbc;

    @Autowired
    private JpaUserRepository repository;

    @Autowired
    private EntityManager entityManager;

    @Test
    void writesAtOnceForPlainSqlInTheSameTransaction() {
        User zed = user(1, "Zed", new ConfirmationToken("token"));
        repository.save(zed);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM app_user WHERE id = ?", Integer.class, id(1).value()))
                .isEqualTo(1);

        repository.delete(zed);
        assertThat(jdbc.queryForObject("SELECT count(*) FROM app_user WHERE id = ?", Integer.class, id(1).value()))
                .isZero();
    }

    @Test
    void keepsAnUnconfirmedAccountWithItsLink() {
        ConfirmationToken token = new ConfirmationToken("token");
        repository.save(user(1, "Zed", token));
        flushAndClear();

        User user = repository.findById(id(1)).orElseThrow();
        assertThat(user.getName()).isEqualTo(new UserName("Zed"));
        assertThat(user.getEmail()).isEqualTo(new Email("zed@example.com"));
        assertThat(user.getPasswordHash()).isEqualTo(new PasswordHash("hash"));
        assertThat(user.getRegisteredAt()).isEqualTo(NOW);
        assertThat(user.getStatus()).isEqualTo(UserStatus.UNCONFIRMED);
        assertThat(user.getGroups()).isEmpty();
        assertThat(user.getConfirmation())
                .isEqualTo(new PendingConfirmation(token.digest(), NOW.plus(Duration.ofDays(7))));
        assertThat(repository.findByConfirmation(token.digest()))
                .hasValueSatisfying(found -> assertThat(found.getId()).isEqualTo(id(1)));
    }

    @Test
    void keepsAConfirmedAccountWithItsGroups() {
        ConfirmationToken token = new ConfirmationToken("token");
        User user = user(1, "Zed", token);
        user.confirm(token, NOW);
        user.grant(UserGroup.ADMIN);
        repository.save(user);
        flushAndClear();

        User found = repository.findById(id(1)).orElseThrow();
        assertThat(found.getStatus()).isEqualTo(UserStatus.CONFIRMED);
        assertThat(found.getGroups()).containsExactlyInAnyOrder(UserGroup.ADMIN, UserGroup.MEMBER);
        assertThat(found.getConfirmation()).isNull();
        assertThat(repository.findByConfirmation(token.digest())).isEmpty();
        assertThat(repository.hasAdmin()).isTrue();
    }

    @Test
    void keepsTheGroupsAnAdministratorChose() {
        ConfirmationToken token = new ConfirmationToken("token");
        User user = user(1, "Zed", token);
        user.confirm(token, NOW);
        user.grant(UserGroup.ADMIN);
        repository.save(user);
        flushAndClear();

        User found = repository.findById(id(1)).orElseThrow();
        found.changeGroups(new GrantedGroups(Set.of(UserGroup.MODERATOR)));
        repository.save(found);
        flushAndClear();

        assertThat(repository.findById(id(1)).orElseThrow().getGroups())
                .containsExactlyInAnyOrder(UserGroup.MEMBER, UserGroup.MODERATOR);
    }

    @Test
    void tellsWhetherAnotherAccountIsAnAdministrator() {
        ConfirmationToken zedToken = new ConfirmationToken("zed");
        User zed = user(1, "Zed", zedToken);
        zed.confirm(zedToken, NOW);
        zed.grant(UserGroup.ADMIN);
        repository.save(zed);
        ConfirmationToken axeToken = new ConfirmationToken("axe");
        User axe = user(2, "Axe", axeToken);
        axe.confirm(axeToken, NOW);
        repository.save(axe);
        flushAndClear();

        assertThat(repository.hasAdminBesides(id(1))).isFalse();
        assertThat(repository.hasAdminBesides(id(2))).isTrue();
    }

    @Test
    void findsAnAccountByItsNameOrEmailWhateverTheCase() {
        repository.save(user(1, "Zed", new ConfirmationToken("token")));
        flushAndClear();

        assertThat(repository.findByLogin(new Login("zED"))).isPresent();
        assertThat(repository.findByLogin(new Login("ZED@example.COM"))).isPresent();
        assertThat(repository.findByLogin(new Login("Axe"))).isEmpty();
        assertThat(repository.findByName(new UserName("zed"))).isPresent();
        assertThat(repository.findByEmail(new Email("Zed@Example.com"))).isPresent();
        assertThat(repository.hasAdmin()).isFalse();
    }

    @Test
    void aUserNameBelongsToOneAccountWhateverTheCase() {
        repository.save(user(1, "Zed", new ConfirmationToken("a")));
        flushAndClear();

        assertThatThrownBy(() -> repository.save(User.register(
                        id(2),
                        new UserName("ZED"),
                        new Email("other@example.com"),
                        new PasswordHash("hash"),
                        new ConfirmationToken("b"),
                        NOW,
                        Language.EN)))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void deletesAnAccount() {
        repository.save(user(1, "Zed", new ConfirmationToken("token")));
        flushAndClear();

        repository.delete(repository.findById(id(1)).orElseThrow());
        flushAndClear();

        assertThat(repository.findById(id(1))).isEmpty();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }

    private static User user(int n, String name, ConfirmationToken token) {
        return User.register(
                id(n),
                new UserName(name),
                new Email(name.toLowerCase(Locale.ROOT) + "@example.com"),
                new PasswordHash("hash"),
                token,
                NOW,
                Language.EN);
    }

    private static UserId id(int n) {
        return new UserId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }
}
