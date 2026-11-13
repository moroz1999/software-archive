package com.archive.backend.user.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;

public interface SpringDataSessionRepository extends JpaRepository<SessionEntity, String> {

    /** Locks the session, so two refreshes with one secret cannot both succeed. */
    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<SessionEntity> findById(String digest);
}
