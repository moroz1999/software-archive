package com.archive.backend.software.read;

import java.util.List;
import java.util.Set;

/** Finds people, groups and their labels for a software form. */
public interface SoftwareReferenceLookup {

    List<SoftwareReferenceOption> find(String query, Set<String> types, int limit);
}
