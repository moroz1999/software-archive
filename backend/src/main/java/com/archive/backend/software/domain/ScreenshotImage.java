package com.archive.backend.software.domain;

import com.archive.backend.shared.domain.ddd.ValueObject;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;

/**
 * The picture of a screenshot as uploaded, kept byte for byte: a PNG, JPEG, GIF or WebP by its content, of at most
 * {@link #MAX_BYTES} (docs/domain/software.md#screenshots). It is judged by its first bytes and its size; the whole
 * picture is only streamed to where it is kept.
 */
@ValueObject
public final class ScreenshotImage {

    public static final int MAX_BYTES = 10 * 1024 * 1024;

    /** As many first bytes as the longest signature needs: WebP names itself at bytes 8 to 11. */
    public static final int HEAD_BYTES = 12;

    private static final byte[] PNG_SIGNATURE = {(byte) 0x89, 'P', 'N', 'G', '\r', '\n', 0x1A, '\n'};
    private static final byte[] JPEG_SIGNATURE = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF};

    private final ScreenshotFormat format;
    private final ScreenshotFileName name;
    private final long size;
    private final ScreenshotContent content;

    private ScreenshotImage(ScreenshotFormat format, ScreenshotFileName name, long size, ScreenshotContent content) {
        this.format = format;
        this.name = name;
        this.size = size;
        this.content = content;
    }

    /**
     * The picture of the content, its format told by its first bytes ({@link #HEAD_BYTES} of them, or all when it is
     * shorter) and its size checked before anything is read further.
     */
    public static ScreenshotImage of(byte[] head, long size, ScreenshotFileName name, ScreenshotContent content) {
        if (size > MAX_BYTES) {
            throw new ScreenshotTooLargeException();
        }
        return new ScreenshotImage(formatOf(head), name, size, content);
    }

    /** The format by the signature the picture starts with. */
    private static ScreenshotFormat formatOf(byte[] bytes) {
        if (startsWith(bytes, 0, PNG_SIGNATURE)) {
            return ScreenshotFormat.PNG;
        }
        if (startsWith(bytes, 0, JPEG_SIGNATURE)) {
            return ScreenshotFormat.JPEG;
        }
        if (startsWith(bytes, 0, ascii("GIF87a")) || startsWith(bytes, 0, ascii("GIF89a"))) {
            return ScreenshotFormat.GIF;
        }
        if (startsWith(bytes, 0, ascii("RIFF")) && startsWith(bytes, 8, ascii("WEBP"))) {
            return ScreenshotFormat.WEBP;
        }
        throw new ScreenshotFormatUnsupportedException();
    }

    private static boolean startsWith(byte[] bytes, int offset, byte[] signature) {
        return bytes.length >= offset + signature.length
                && Arrays.equals(bytes, offset, offset + signature.length, signature, 0, signature.length);
    }

    private static byte[] ascii(String text) {
        return text.getBytes(StandardCharsets.US_ASCII);
    }

    public ScreenshotFormat format() {
        return format;
    }

    /** The name of the file the picture came in. */
    public ScreenshotFileName name() {
        return name;
    }

    /** The size of the picture in bytes. */
    public long size() {
        return size;
    }

    /** The bytes of the picture as uploaded. */
    public ScreenshotContent content() {
        return content;
    }
}
