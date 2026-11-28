package com.archive.backend.remote.app;

import com.archive.backend.remote.read.RemoteResources;
import com.archive.backend.remote.read.ViewedImage;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A picture of another site to look at: a PNG, JPEG, GIF, WebP or BMP of at most 5 MB. */
@Service
@RequiredArgsConstructor
public class ViewImageHandler {

    static final int MAX_BYTES = 5 * 1024 * 1024;

    private final RemoteResources remote;

    @Transactional(readOnly = true)
    public ViewedImage handle(ViewImageQuery q) {
        return remote.viewImage(q.url().value(), MAX_BYTES);
    }
}
