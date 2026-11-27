package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public enum SoftwareLinkType {
    GENERAL("general"),
    HOMEPAGE("homepage"),
    STORE("store"),
    YOUTUBE("youtube");

    private final String code;

    SoftwareLinkType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }
}
