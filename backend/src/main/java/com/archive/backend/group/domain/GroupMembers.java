package com.archive.backend.group.domain;

import com.archive.backend.author.domain.PersonReference;
import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/** The complete roster owned by one group. */
@ValueObject
public final class GroupMembers {

    public static final GroupMembers EMPTY = new GroupMembers(List.of());

    private final List<GroupMemberDraft> members;

    private GroupMembers(List<GroupMemberDraft> members) {
        this.members = List.copyOf(members);
        Set<PersonReference> people = new HashSet<>();
        if (members.stream().anyMatch(member -> !people.add(member.person()))) {
            throw new InvalidValueException(
                    "group.member-duplicate", "A person can occur in a group roster only once.");
        }
    }

    static GroupMembers of(List<GroupMemberDraft> members) {
        return new GroupMembers(members);
    }

    public static GroupMembers restore(List<GroupMemberDraft> members) {
        return new GroupMembers(members);
    }

    public List<GroupMemberDraft> all() {
        return members;
    }
}
