package com.archive.backend.user.app;

import com.archive.backend.user.domain.session.RefreshSecret;
import com.archive.backend.user.domain.session.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LogOutHandler {

    private final SessionRepository sessions;

    @Transactional
    public void handle(LogOutCommand c) {
        RefreshSecret secret = c.secret();
        if (secret != null) {
            sessions.findByDigest(secret.digest()).ifPresent(sessions::delete);
        }
    }
}
