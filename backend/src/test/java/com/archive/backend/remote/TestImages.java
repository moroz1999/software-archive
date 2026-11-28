package com.archive.backend.remote;

import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.UncheckedIOException;
import javax.imageio.ImageIO;

/** Real pictures for the tests. */
public final class TestImages {

    private TestImages() {}

    /** A PNG of the size given. */
    public static byte[] png(int width, int height) {
        return image("png", width, height);
    }

    /** A BMP of the size given. */
    public static byte[] bmp(int width, int height) {
        return image("bmp", width, height);
    }

    private static byte[] image(String format, int width, int height) {
        try {
            ByteArrayOutputStream out = new ByteArrayOutputStream();
            ImageIO.write(new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB), format, out);
            return out.toByteArray();
        } catch (IOException e) {
            throw new UncheckedIOException(e);
        }
    }
}
