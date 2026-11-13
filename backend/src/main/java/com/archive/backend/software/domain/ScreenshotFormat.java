package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import com.archive.backend.shared.domain.error.InvalidValueException;
import java.util.Arrays;

/** The picture formats a screenshot may be in (docs/domain/software.md#screenshots), each known by its code. */
@ValueObject
public enum ScreenshotFormat {
    PNG("png"),
    JPEG("jpeg"),
    GIF("gif"),
    WEBP("webp");

    private final String code;

    ScreenshotFormat(String code) {
        this.code = code;
    }

    public String code() {
        return code;
    }

    public static ScreenshotFormat fromCode(String code) {
        return Arrays.stream(values())
                .filter(format -> format.code.equals(code))
                .findFirst()
                .orElseThrow(() ->
                        new InvalidValueException("screenshot.format-invalid", "The screenshot format is unknown."));
    }
}
