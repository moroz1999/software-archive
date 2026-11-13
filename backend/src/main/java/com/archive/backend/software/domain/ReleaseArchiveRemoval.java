package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;

/** Whether the current release archive should be removed. */
@ValueObject
public record ReleaseArchiveRemoval(boolean value) {}
