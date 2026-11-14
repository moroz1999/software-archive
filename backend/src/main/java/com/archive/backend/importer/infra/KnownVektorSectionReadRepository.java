package com.archive.backend.importer.infra;

import com.archive.backend.importer.domain.vektor.VektorSection;
import com.archive.backend.importer.domain.vektor.VektorSections;
import com.archive.backend.importer.read.VektorSectionReadRepository;
import java.util.List;
import org.springframework.stereotype.Repository;

/** The sections the import knows how to place: they are part of the code, not of the database. */
@Repository
public class KnownVektorSectionReadRepository implements VektorSectionReadRepository {

    @Override
    public List<String> findTaken() {
        return VektorSections.taken().stream().map(VektorSection::code).toList();
    }
}
