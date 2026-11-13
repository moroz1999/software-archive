package com.archive.backend.user.app;

import com.archive.backend.user.domain.TokenDigest;
import com.archive.backend.user.domain.session.Session;
import com.archive.backend.user.domain.session.SessionRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class InMemorySessionRepository implements SessionRepository {

    private final Map<TokenDigest, Session> items = new LinkedHashMap<>();

    int count() {
        return items.size();
    }

    @Override
    public Optional<Session> findByDigest(TokenDigest digest) {
        return Optional.ofNullable(items.get(digest));
    }

    @Override
    public void save(Session session) {
        items.put(session.getDigest(), session);
    }

    @Override
    public void delete(Session session) {
        items.remove(session.getDigest());
    }
}
