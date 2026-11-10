package com.archive.backend.software.app;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.read.SoftwarePlatform;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class GetSoftwareReleaseHandlerTest {

    private static final UUID ELITE = UUID.fromString("00000000-0000-0000-0000-000000000001");
    private static final UUID AY_MOD = UUID.fromString("00000000-0000-0000-0000-000000000012");
    private static final ReleaseDetails AY_MOD_DETAILS = new ReleaseDetails(
            AY_MOD, null, "1.1", "mod", null, null, List.of(new SoftwareHardware("ay8910", "AY-3-8910", "sound")));

    private final InMemorySoftwareReadRepository reads = new InMemorySoftwareReadRepository();
    private final GetSoftwareReleaseHandler handler = new GetSoftwareReleaseHandler(reads);

    private void eliteWithAyMod() {
        reads.add(new SoftwareDetails(ELITE, "Elite", null, null, new SoftwarePlatform("vector", "Vector"), List.of()));
        reads.addRelease(ELITE, AY_MOD_DETAILS);
    }

    @Test
    void returnsTheReleaseAsItStatesItself() {
        eliteWithAyMod();

        assertThat(handler.handle(new GetSoftwareReleaseQuery(new SoftwareId(ELITE), new ReleaseId(AY_MOD))))
                .isEqualTo(AY_MOD_DETAILS);
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(
                        () -> handler.handle(new GetSoftwareReleaseQuery(new SoftwareId(ELITE), new ReleaseId(AY_MOD))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void failsWhenTheSoftwareHasNoSuchRelease() {
        eliteWithAyMod();

        assertThatThrownBy(() -> handler.handle(
                        new GetSoftwareReleaseQuery(new SoftwareId(ELITE), new ReleaseId(UUID.randomUUID()))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("release.not-found"));
    }
}
