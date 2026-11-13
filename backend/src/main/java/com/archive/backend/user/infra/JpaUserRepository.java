package com.archive.backend.user.infra;

import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.PasswordHash;
import com.archive.backend.user.domain.PendingConfirmation;
import com.archive.backend.user.domain.TokenDigest;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserRepository;
import com.archive.backend.user.domain.UserStatus;
import java.time.Instant;
import java.util.HashSet;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaUserRepository implements UserRepository {

    private final SpringDataUserRepository springData;

    @Override
    public Optional<User> findById(UserId id) {
        return springData.findById(id.value()).map(JpaUserRepository::toDomain);
    }

    @Override
    public Optional<User> findByLogin(Login login) {
        return springData.findByLogin(login.value()).map(JpaUserRepository::toDomain);
    }

    @Override
    public Optional<User> findByName(UserName name) {
        return springData.findByNameIgnoringCase(name.value()).map(JpaUserRepository::toDomain);
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return springData.findByEmail(email.value()).map(JpaUserRepository::toDomain);
    }

    @Override
    public Optional<User> findByConfirmation(TokenDigest digest) {
        return springData.findByConfirmationDigest(digest.value()).map(JpaUserRepository::toDomain);
    }

    @Override
    public boolean hasAdmin() {
        return springData.existsInGroup(UserGroup.ADMIN.code());
    }

    @Override
    public void save(User user) {
        springData.save(toEntity(user));
    }

    /** Its sessions go with it (the schema cascades). */
    @Override
    public void delete(User user) {
        springData.deleteById(user.getId().value());
        springData.flush();
    }

    private static User toDomain(UserEntity entity) {
        String digest = entity.getConfirmationDigest();
        Instant expiresAt = entity.getConfirmationExpiresAt();
        return new User(
                new UserId(entity.getId()),
                new UserName(entity.getName()),
                new Email(entity.getEmail()),
                new PasswordHash(entity.getPasswordHash()),
                entity.getRegisteredAt(),
                UserStatus.valueOf(entity.getStatus()),
                entity.getGroups().stream().map(UserGroup::ofCode).collect(Collectors.toSet()),
                digest == null || expiresAt == null
                        ? null
                        : new PendingConfirmation(new TokenDigest(digest), expiresAt));
    }

    private static UserEntity toEntity(User user) {
        UserEntity entity = new UserEntity();
        entity.setId(user.getId().value());
        entity.setName(user.getName().value());
        entity.setEmail(user.getEmail().value());
        entity.setPasswordHash(user.getPasswordHash().value());
        entity.setRegisteredAt(user.getRegisteredAt());
        entity.setStatus(user.getStatus().name());
        PendingConfirmation confirmation = user.getConfirmation();
        entity.setConfirmationDigest(
                confirmation == null ? null : confirmation.digest().value());
        entity.setConfirmationExpiresAt(confirmation == null ? null : confirmation.expiresAt());
        entity.setGroups(user.getGroups().stream().map(UserGroup::code).collect(Collectors.toCollection(HashSet::new)));
        return entity;
    }
}
