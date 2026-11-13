package com.archive.backend.user.infra;

import com.archive.backend.user.domain.TokenDigest;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.session.RememberMe;
import com.archive.backend.user.domain.session.Session;
import com.archive.backend.user.domain.session.SessionRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaSessionRepository implements SessionRepository {

    private final SpringDataSessionRepository springData;

    @Override
    public Optional<Session> findByDigest(TokenDigest digest) {
        return springData
                .findById(digest.value())
                .map(entity -> new Session(
                        new TokenDigest(entity.getDigest()),
                        new UserId(entity.getUserId()),
                        entity.getExpiresAt(),
                        new RememberMe(entity.isRemember())));
    }

    @Override
    public void save(Session session) {
        SessionEntity entity = new SessionEntity();
        entity.setDigest(session.getDigest().value());
        entity.setUserId(session.getUserId().value());
        entity.setExpiresAt(session.getExpiresAt());
        entity.setRemember(session.getRemember().value());
        springData.saveAndFlush(entity);
    }

    @Override
    public void delete(Session session) {
        springData.deleteById(session.getDigest().value());
        springData.flush();
    }
}
