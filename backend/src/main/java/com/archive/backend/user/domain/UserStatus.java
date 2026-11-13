package com.archive.backend.user.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

@ValueObject
public enum UserStatus {
    UNCONFIRMED,
    CONFIRMED
}
