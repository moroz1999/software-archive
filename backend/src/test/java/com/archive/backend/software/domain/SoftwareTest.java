package com.archive.backend.software.domain;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import java.time.LocalDate;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SoftwareTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");
    private static final PlatformCode BK = new PlatformCode("bk");

    @Test
    void isCreatedWithItsTitleDescriptionReleaseDateAndPlatform() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                VECTOR);

        assertThat(software.getId()).isEqualTo(ID);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite"));
        assertThat(software.getDescription()).isEqualTo(new SoftwareDescription("Space trading"));
        assertThat(software.getReleaseDate()).isEqualTo(LocalDate.of(1985, 9, 20));
        assertThat(software.getPlatform()).isEqualTo(VECTOR);
    }

    @Test
    void editingReplacesTitleDescriptionReleaseDateAndPlatform() {
        Software software = Software.create(
                ID,
                new SoftwareTitle("Elite"),
                new SoftwareDescription("Space trading"),
                LocalDate.of(1985, 9, 20),
                VECTOR);

        software.edit(new SoftwareTitle("Elite Plus"), null, null, BK);

        assertThat(software.getId()).isEqualTo(ID);
        assertThat(software.getTitle()).isEqualTo(new SoftwareTitle("Elite Plus"));
        assertThat(software.getDescription()).isNull();
        assertThat(software.getReleaseDate()).isNull();
        assertThat(software.getPlatform()).isEqualTo(BK);
    }
}
