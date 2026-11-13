package com.archive.backend.user.domain.session;

import com.archive.backend.user.domain.TokenDigest;
import java.util.Optional;

public interface SessionRepository {

    Optional<Session> findByDigest(TokenDigest digest);

    void save(Session session);

    void delete(Session session);
}
