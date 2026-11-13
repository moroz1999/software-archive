package com.archive.backend.user.app;

import static com.archive.backend.user.app.UserFixtures.ACCESS_TOKENS;
import static com.archive.backend.user.app.UserFixtures.NOW;
import static com.archive.backend.user.app.UserFixtures.id;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.Sessions;
import org.junit.jupiter.api.Test;

class LogOutHandlerTest {

    private final InMemoryUserRepository users = new InMemoryUserRepository();
    private final InMemorySessionRepository sessionRepository = new InMemorySessionRepository();
    private final Sessions sessions = new Sessions(sessionRepository, users, ACCESS_TOKENS);
    private final LogOutHandler handler = new LogOutHandler(sessionRepository);

    @Test
    void endsTheSession() {
        users.save(UserFixtures.confirmed(1, "Zed"));
        IssuedSession opened = sessions.open(users.findById(id(1)).orElseThrow(), new RememberMe(true), NOW);

        handler.handle(new LogOutCommand(opened.refresh()));

        assertThat(sessionRepository.findByDigest(opened.refresh().digest())).isEmpty();
    }

    @Test
    void aBrowserWithoutASessionHasNothingToEnd() {
        assertThatCode(() -> handler.handle(new LogOutCommand(null))).doesNotThrowAnyException();
        assertThatCode(() -> handler.handle(new LogOutCommand(new RefreshSecret("unknown"))))
                .doesNotThrowAnyException();
    }
}
