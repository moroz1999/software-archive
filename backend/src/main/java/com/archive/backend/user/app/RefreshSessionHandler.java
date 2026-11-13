package com.archive.backend.user.app;

import com.archive.backend.user.domain.session.IssuedSession;
import com.archive.backend.user.domain.session.Sessions;
import java.time.Clock;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class RefreshSessionHandler {

    private final Sessions sessions;
    private final Clock clock;

    @Transactional
    public IssuedSession handle(RefreshSessionCommand c) {
        return sessions.refresh(c.secret(), clock.instant());
    }
}
