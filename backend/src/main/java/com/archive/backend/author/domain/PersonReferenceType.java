package com.archive.backend.author.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;

@ValueObject
public enum PersonReferenceType {
    AUTHOR("author"),
    AUTHOR_LABEL("author-label");

    private final String code;

    PersonReferenceType(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static PersonReferenceType fromCode(String code) {
        for (PersonReferenceType type : values()) {
            if (type.code.equals(code)) {
                return type;
            }
        }
        throw new InvalidValueException("person.reference-type-invalid", "A person reference type is invalid.");
    }
}
