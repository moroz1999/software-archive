package com.archive.backend.group.domain;

import com.archive.backend.author.domain.PersonReferenceCatalog;
import java.util.List;

/** Builds a roster only from people and aliases that still exist in the archive. */
public class GroupMemberSets {

    private final PersonReferenceCatalog catalog;

    public GroupMemberSets(PersonReferenceCatalog catalog) {
        this.catalog = catalog;
    }

    public GroupMembers of(List<GroupMemberDraft> members) {
        if (members.stream().anyMatch(member -> !catalog.contains(member.person()))) {
            throw new GroupMemberUnknownException();
        }
        return GroupMembers.of(members);
    }
}
