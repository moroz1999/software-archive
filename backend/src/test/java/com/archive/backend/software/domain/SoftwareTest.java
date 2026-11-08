package com.archive.backend.software.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SoftwareTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));

    @Test
    void isCreatedWithItsTitleDescriptionAndReleaseDate() {
        Software software = Software.create(ID, new SoftwareTitle("Elite"), new SoftwareDescription("Space trading"), LocalDate.of(1985, 9, 20));

        assertThat(software.getId()).isEqualTo(ID);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
        assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
        assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
    }

    @Test
    void editingReplacesTitleDescriptionAndReleaseDate() {
        Software software = Software.create(ID, new SoftwareTitle("Elite"), new SoftwareDescription("Space trading"), LocalDate.of(1985, 9, 20));

        software.edit(new SoftwareTitle("Elite Plus"), null, null);

        assertThat(software.getId()).isEqualTo(ID);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
        assertThat(software.getDescription()).isNull();
        assertThat(software.getReleaseDate()).isNull();
    }
}
