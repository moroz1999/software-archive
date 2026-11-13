package com.archive.backend.user.infra;

import jakarta.persistence.LockModeType;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * Accounts are loaded only to change them: each find locks the row until the transaction commits (docs/backend.md).
 */
public interface SpringDataUserRepository extends JpaRepository<UserEntity, UUID> {

    @Override
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findById(UUID id);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where lower(u.name) = lower(:login) or u.email = lower(:login)")
    Optional<UserEntity> findByLogin(@Param("login") String login);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select u from UserEntity u where lower(u.name) = lower(:name)")
    Optional<UserEntity> findByNameIgnoringCase(@Param("name") String name);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findByEmail(String email);

    @Lock(LockModeType.PESSIMISTIC_WRITE)
    Optional<UserEntity> findByConfirmationDigest(String digest);

    @Query("select count(u) > 0 from UserEntity u join u.groups g where g = :group")
    boolean existsInGroup(@Param("group") String group);
}
