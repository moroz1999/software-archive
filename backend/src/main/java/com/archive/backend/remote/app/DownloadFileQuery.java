package com.archive.backend.remote.app;

import com.archive.backend.remote.domain.RemoteByteLimit;
import com.archive.backend.remote.domain.RemoteUrl;

/** A file of another site, downloaded to be stored. */
public record DownloadFileQuery(RemoteUrl url, RemoteByteLimit limit) {}
