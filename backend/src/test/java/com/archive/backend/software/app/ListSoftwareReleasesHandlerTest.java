package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.tuple;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import com.archive.backend.software.read.SoftwareRelease;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class ListSoftwareReleasesHandlerTest {

    private static final UUID ID = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final SoftwareHardware VECTOR = new SoftwareHardware("vector06c", "Vector-06C", "computers");
    private static final SoftwareHardware BEEPER = new SoftwareHardware("beeper", "Beeper", "sound");
    private static final SoftwareHardware AY = new SoftwareHardware("ay8910", "AY-3-8910", "sound");

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final ListSoftwareReleasesHandler handler = new ListSoftwareReleasesHandler(reads);

    @Test
    void returnsTheReleasesWithTheHardwareTheyShowOverTheSoftware() {
        reads.add(new SoftwareDetails(
                ID, "Elite", null, null, new SoftwarePlatform("vector", "Vector"), List.of(VECTOR, BEEPER), List.of()));
        reads.addReleases(ID, release("Elite", List.of()), release("Elite AY", List.of(AY)));

        assertThat(handler.handle(new ListSoftwareReleasesQuery(new SoftwareId(ID))))
                .extracting(SoftwareRelease::title, SoftwareRelease::hardware)
                .containsExactly(tuple("Elite", List.of(VECTOR, BEEPER)), tuple("Elite AY", List.of(VECTOR, AY)));
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new ListSoftwareReleasesQuery(new SoftwareId(ID))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    private static SoftwareRelease release(String title, List<SoftwareHardware> own) {
        return new SoftwareRelease(UUID.randomUUID(), title, null, "original", null, null, own);
    }
}
