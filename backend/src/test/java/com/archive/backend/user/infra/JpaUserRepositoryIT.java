package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
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
import java.util.UUID;
import org.hibernate.exception.ConstraintViolationException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaUserRepository.class})
class JpaUserRepositoryIT {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");

    @Autowired
    private JpaUserRepository repository;

    @Autowired
    private EntityManager entityManager;

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

        assertThatThrownBy(() -> {
                    repository.save(User.register(
                            id(2),
                            new UserName("ZED"),
                            new Email("other@example.com"),
                            new PasswordHash("hash"),
                            new ConfirmationToken("b"),
                            NOW,
                            InterfaceLanguage.EN));
                    entityManager.flush();
                })
                .isInstanceOf(ConstraintViolationException.class);
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
                InterfaceLanguage.EN);
    }

    private static UserId id(int n) {
        return new UserId(UUID.fromString("00000000-0000-0000-0000-%012d".formatted(n)));
    }
}
