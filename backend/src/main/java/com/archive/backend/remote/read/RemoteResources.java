package com.archive.backend.remote.read;

/** What another site serves at an address given from outside, read only through the guard of the archive's network. */
public interface RemoteResources {

    FetchedPage fetchPage(String url, int maxBytes);

    /** A PNG, JPEG, GIF, WebP or BMP of at most {@code maxBytes}; anything else is refused. */
    ViewedImage viewImage(String url, int maxBytes);

    /** The file into a temporary one, refused past {@code maxBytes} or on a status other than success. */
    DownloadedFile download(String url, long maxBytes);
}
