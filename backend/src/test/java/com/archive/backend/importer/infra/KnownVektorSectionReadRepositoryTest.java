package com.archive.backend.importer.infra;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class KnownVektorSectionReadRepositoryTest {

    @Test
    void givesTheCodesOfTheSectionsTheImportTakes() {
        assertThat(new KnownVektorSectionReadRepository().findTaken())
                .contains("arkadnye-igry", "zagruzchiki")
                .doesNotContain("utility-dlya-pc")
                .isSorted();
    }
}
