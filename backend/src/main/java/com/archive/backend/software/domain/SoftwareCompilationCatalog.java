package com.archive.backend.software.domain;

import java.util.Set;

public interface SoftwareCompilationCatalog {

    /** Serializes changes that can add links until the transaction ends. */
    void lockChanges();

    /** Checks the references and protects them from deletion until the transaction ends. */
    boolean allExist(Set<SoftwareId> ids);

    /** Whether any of the programs already includes the compilation, directly or through other compilations. */
    boolean includes(SoftwareId compilation, Set<SoftwareId> programs);
}
