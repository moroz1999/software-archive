package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.Title;
import com.archive.backend.shared.domain.ddd.ValueObject;

/**
 * The own title of a release; a release without one is called by the title of its software.
 */
@ValueObject
public final class ReleaseTitle extends Title {

    public ReleaseTitle(String value) {
        super(value);
    }
}
