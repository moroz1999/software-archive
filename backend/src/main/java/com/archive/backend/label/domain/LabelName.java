package com.archive.backend.label.domain;

import com.archive.backend.shared.domain.Title;
import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * A name works are released under.
 */
@ValueObject
public final class LabelName extends Title {

    public LabelName(String value) {
        super(value);
    }
}
