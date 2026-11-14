package com.archive.backend.importer.read;

import java.util.List;

/** The sections of vektor-06c.ru an import can be started for. */
public interface VektorSectionReadRepository {

    /** The codes of the sections whose programs the archive takes, in alphabetical order. */
    List<String> findTaken();
}
