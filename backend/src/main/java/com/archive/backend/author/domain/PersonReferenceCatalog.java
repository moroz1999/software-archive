package com.archive.backend.author.domain;

/** People and their labels that may be selected in another aggregate. */
public interface PersonReferenceCatalog {
    boolean contains(PersonReference reference);
}
