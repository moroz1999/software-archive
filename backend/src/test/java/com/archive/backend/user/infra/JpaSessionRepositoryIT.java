package com.archive.backend.user.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.user.domain.ConfirmationToken;
import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.InterfaceLanguage;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.Session;
import jakarta.persistence.EntityManager;
import java.time.Duration;
import java.time.Instant;
import java.util.UUID;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JpaUserRepository.class, JpaSessionRepository.class})
class JpaSessionRepositoryIT {

    private static final Instant NOW = Instant.parse("2026-01-01T00:00:00Z");
    private static final UserId ZED = new UserId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final RefreshSecret SECRET = new RefreshSecret("secret");

    @Autowired
    private JpaSessionRepository repository;

    @Autowired
    private JpaUserRepository users;

    @Autowired
    private EntityManager entityManager;

    @BeforeEach
    void insertUser() {
        users.save(User.register(
                ZED,
                new UserName("Zed"),
                new Email("zed@example.com"),
                new PasswordHash("hash"),
                new ConfirmationToken("token"),
                NOW,
                InterfaceLanguage.EN));
        entityManager.flush();
    }

    @Test
    void keepsASessionByTheDigestOfItsSecret() {
        repository.save(Session.open(SECRET, ZED, new RememberMe(false), NOW));
        flushAndClear();

        Session session = repository.findByDigest(SECRET.digest()).orElseThrow();
        assertThat(session.getUserId()).isEqualTo(ZED);
        assertThat(session.getExpiresAt()).isEqualTo(NOW.plus(Duration.ofDays(30)));
        assertThat(session.getRemember()).isEqualTo(new RememberMe(false));
    }

    @Test
    void deletesASession() {
        repository.save(Session.open(SECRET, ZED, new RememberMe(true), NOW));
        flushAndClear();

        repository.delete(repository.findByDigest(SECRET.digest()).orElseThrow());
        flushAndClear();

        assertThat(repository.findByDigest(SECRET.digest())).isEmpty();
    }

    @Test
    void sessionsGoWithTheirAccount() {
        repository.save(Session.open(SECRET, ZED, new RememberMe(true), NOW));
        flushAndClear();

        users.delete(users.findById(ZED).orElseThrow());
        flushAndClear();

        assertThat(repository.findByDigest(SECRET.digest())).isEmpty();
    }

    private void flushAndClear() {
        entityManager.flush();
        entityManager.clear();
    }
}
