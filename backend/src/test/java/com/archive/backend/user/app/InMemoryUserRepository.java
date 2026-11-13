package com.archive.backend.user.app;

import com.archive.backend.user.domain.Email;
import com.archive.backend.user.domain.Login;
import com.archive.backend.user.domain.PendingConfirmation;
import com.archive.backend.user.domain.TokenDigest;
import com.archive.backend.user.domain.User;
import com.archive.backend.user.domain.UserGroup;
import com.archive.backend.user.domain.UserId;
import com.archive.backend.user.domain.UserName;
import com.archive.backend.user.domain.UserRepository;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;

class InMemoryUserRepository implements UserRepository {

    private final Map<UserId, User> items = new LinkedHashMap<>();

    int count() {
        return items.size();
    }

    @Override
    public Optional<User> findById(UserId id) {
        return Optional.ofNullable(items.get(id));
    }

    @Override
    public Optional<User> findByLogin(Login login) {
        return items.values().stream()
                .filter(user -> user.getName().value().equalsIgnoreCase(login.value())
                        || user.getEmail().value().equalsIgnoreCase(login.value()))
                .findFirst();
    }

    @Override
    public Optional<User> findByName(UserName name) {
        return items.values().stream()
                .filter(user -> user.getName().value().equalsIgnoreCase(name.value()))
                .findFirst();
    }

    @Override
    public Optional<User> findByEmail(Email email) {
        return items.values().stream()
                .filter(user -> user.getEmail().equals(email))
                .findFirst();
    }

    @Override
    public Optional<User> findByConfirmation(TokenDigest digest) {
        return items.values().stream()
                .filter(user -> {
                    PendingConfirmation confirmation = user.getConfirmation();
                    return confirmation != null && confirmation.digest().equals(digest);
                })
                .findFirst();
    }

    @Override
    public boolean hasAdmin() {
        return items.values().stream().anyMatch(user -> user.getGroups().contains(UserGroup.ADMIN));
    }

    @Override
    public void save(User user) {
        items.put(user.getId(), user);
    }

    @Override
    public void delete(User user) {
        items.remove(user.getId());
    }
}
