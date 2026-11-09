package com.archive.backend.hardware.domain;

/** The platforms of the archive, for the use cases that need to know one exists. */
public interface PlatformRepository {

    boolean exists(PlatformCode code);
}
