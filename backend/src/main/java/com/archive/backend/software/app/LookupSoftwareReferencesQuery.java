package com.archive.backend.software.app;

import com.archive.backend.software.domain.SoftwareReferencePhrase;
import com.archive.backend.software.domain.SoftwareReferenceType;
import java.util.Set;

/** A typed phrase entered in a software relation picker. */
public record LookupSoftwareReferencesQuery(SoftwareReferencePhrase phrase, Set<SoftwareReferenceType> types) {}
