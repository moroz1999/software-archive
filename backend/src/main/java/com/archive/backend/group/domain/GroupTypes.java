package com.archive.backend.group.domain;

import java.util.Set;

/**
 * Builds the types of a group, checking them against the reference book.
 */
public class GroupTypes {

    private final GroupTypeCatalog catalog;

    public GroupTypes(GroupTypeCatalog catalog) {
        this.catalog = catalog;
    }

    public GroupTypeSet of(Set<GroupTypeCode> codes) {
        if (codes.isEmpty()) {
            throw new GroupTypesRequiredException();
        }
        if (!catalog.allExist(codes)) {
            throw new GroupTypeUnknownException();
        }
        return new GroupTypeSet(codes);
    }
}
