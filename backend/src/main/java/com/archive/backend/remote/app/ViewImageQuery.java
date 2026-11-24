package com.archive.backend.remote.app;

import com.archive.backend.remote.domain.RemoteUrl;

/** A picture of another site, to look at. */
public record ViewImageQuery(RemoteUrl url) {}
