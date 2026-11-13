package com.archive.backend.software.domain;

import java.io.IOException;
import java.io.InputStream;

/** Where an uploaded release archive is streamed from without holding it in memory. */
@FunctionalInterface
public interface ReleaseArchiveContent {

    InputStream open() throws IOException;
}
