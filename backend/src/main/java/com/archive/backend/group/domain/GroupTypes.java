package com.archive.backend.group.domain;

import java.util.Set;

/**
 * Builds the types of a group, checking them against the reference book; a group may be of no known type.
 */
public class GroupTypes {

    private final GroupTypeCatalog catalog;

    public GroupTypes(GroupTypeCatalog catalog) {
        this.catalog = catalog;
    }

    public GroupTypeSet of(Set<GroupTypeCode> codes) {
        if (!catalog.allExist(codes)) {
            throw new GroupTypeUnknownException();
        }
        return new GroupTypeSet(codes);
    }
}
