package com.archive.backend.remote.app;

import com.archive.backend.remote.domain.RemoteUrl;

/** A page of another site, to read. */
public record FetchPageQuery(RemoteUrl url) {}
