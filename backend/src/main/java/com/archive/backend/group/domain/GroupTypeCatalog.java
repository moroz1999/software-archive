package com.archive.backend.group.domain;

import java.util.Set;

/**
 * The reference book of group types, the types of a group are checked against.
 */
@FunctionalInterface
public interface GroupTypeCatalog {

    boolean allExist(Set<GroupTypeCode> codes);
}
