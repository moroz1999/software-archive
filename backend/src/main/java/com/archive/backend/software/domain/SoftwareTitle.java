package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.Title;
import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public final class SoftwareTitle extends Title {

    public SoftwareTitle(String value) {
        super(value);
    }
}
