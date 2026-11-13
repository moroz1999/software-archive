package com.archive.backend.software.domain;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/** Pictures of every format, as tests upload them. */
public final class ScreenshotImages {

    private ScreenshotImages() {}

    public static byte[] png() {
        return new byte[] {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n', 0, 0, 0, 13};
    }

    public static byte[] jpeg() {
        return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0, 16};
    }

    public static byte[] gif() {
        return "GIF89a\u0001\u0000".getBytes(StandardCharsets.ISO_8859_1);
    }

    public static byte[] webp() {
        return "RIFF\u0000\u0000\u0000\u0000WEBPVP8 ".getBytes(StandardCharsets.ISO_8859_1);
    }

    public static byte[] text() {
        return "not a picture at all".getBytes(StandardCharsets.ISO_8859_1);
    }

    /** The picture of the bytes, as an upload hands it over. */
    public static ScreenshotImage image(byte[] bytes) {
        return ScreenshotImage.of(
                Arrays.copyOf(bytes, Math.min(bytes.length, ScreenshotImage.HEAD_BYTES)),
                bytes.length,
                () -> new ByteArrayInputStream(bytes));
    }

    /** A PNG of exactly the size. */
    public static byte[] pngOf(int size) {
        return Arrays.copyOf(png(), size);
    }
}
