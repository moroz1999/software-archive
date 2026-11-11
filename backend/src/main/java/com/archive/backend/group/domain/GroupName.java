package com.archive.backend.group.domain;

import com.archive.backend.shared.domain.Title;
import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * The name of a group, apart from the labels it releases under.
 */
@ValueObject
public final class GroupName extends Title {

    public GroupName(String value) {
        super(value);
    }
}
