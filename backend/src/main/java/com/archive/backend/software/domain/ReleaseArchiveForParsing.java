package com.archive.backend.software.domain;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.ddd.ValueObject;
import java.util.Objects;

@ValueObject
public record ReleaseArchiveForParsing(PlatformCode platform, ReleaseArchiveUpload upload) {

    public ReleaseArchiveForParsing {
        Objects.requireNonNull(platform, "platform");
        Objects.requireNonNull(upload, "upload");
    }
}
