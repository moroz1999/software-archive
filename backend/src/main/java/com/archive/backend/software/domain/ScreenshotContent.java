package com.archive.backend.software.domain;

import java.io.IOException;
import java.io.InputStream;

/** Where the bytes of an uploaded picture are read from, as many times as needed, never held whole in memory. */
@FunctionalInterface
public interface ScreenshotContent {

    InputStream open() throws IOException;
}
