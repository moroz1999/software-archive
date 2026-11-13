package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Collection;
import java.util.EnumSet;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * The groups an administrator gives a user on purpose: administrator and moderator.
 */
@ValueObject
public record GrantedGroups(Set<UserGroup> groups) {

    private static final Set<UserGroup> GIVEN_ON_PURPOSE = EnumSet.of(UserGroup.ADMIN, UserGroup.MODERATOR);

    public GrantedGroups {
        if (!GIVEN_ON_PURPOSE.containsAll(groups)) {
            throw invalid();
        }
        groups = Set.copyOf(groups);
    }

    /**
     * The groups of their codes; an unknown code, or a group not given on purpose, is invalid.
     */
    public static GrantedGroups ofCodes(Collection<String> codes) {
        return new GrantedGroups(codes.stream().map(GrantedGroups::groupOf).collect(Collectors.toSet()));
    }

    public boolean contains(UserGroup group) {
        return groups.contains(group);
    }

    private static UserGroup groupOf(String code) {
        return GIVEN_ON_PURPOSE.stream()
                .filter(group -> group.code().equals(code))
                .findFirst()
                .orElseThrow(GrantedGroups::invalid);
    }

    private static InvalidValueException invalid() {
        return new InvalidValueException(
                "user.group-invalid", "Only administrators and moderators are given on purpose.");
    }
}
