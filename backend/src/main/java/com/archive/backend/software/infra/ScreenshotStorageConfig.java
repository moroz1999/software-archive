package com.archive.backend.software.infra;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableConfigurationProperties({
    ScreenshotStorageProperties.class,
    ReleaseArchiveStorageProperties.class,
    ReleaseFileParserProperties.class
})
public class ScreenshotStorageConfig {}
