package com.archive.backend.remote.app;

import com.archive.backend.remote.read.FetchedPage;
import com.archive.backend.remote.read.RemoteResources;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/** A page of another site, any status, its text cut at 200 KB. */
@Service
@RequiredArgsConstructor
public class FetchPageHandler {

    static final int MAX_BYTES = 200_000;

    private final RemoteResources remote;

    @Transactional(readOnly = true)
    public FetchedPage handle(FetchPageQuery q) {
        return remote.fetchPage(q.url().value(), MAX_BYTES);
    }
}
