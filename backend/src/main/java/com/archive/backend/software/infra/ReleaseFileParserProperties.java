package com.archive.backend.software.infra;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties("release-files")
public record ReleaseFileParserProperties(long maxExpandedBytes, int maxContainerDepth) {

    public ReleaseFileParserProperties {
        if (maxExpandedBytes <= 0 || maxContainerDepth <= 0) {
            throw new IllegalArgumentException("Release file parser limits must be positive.");
        }
    }
}
