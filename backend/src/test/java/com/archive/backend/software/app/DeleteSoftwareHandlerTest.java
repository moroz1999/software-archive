package com.archive.backend.software.app;

import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.BY_OTHER;
import static com.archive.backend.user.TestActors.MEMBER;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.archive.backend.category.domain.CategorySet;
import com.archive.backend.hardware.domain.HardwareSet;
import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.software.domain.Software;
import com.archive.backend.software.domain.SoftwareCredits;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.domain.SoftwareTitle;
import com.archive.backend.user.domain.ChangeForbiddenException;
import java.util.Set;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DeleteSoftwareHandlerTest {

    private static final SoftwareId ID = new SoftwareId(UUID.fromString("00000000-0000-0000-0000-000000000001"));
    private static final PlatformCode VECTOR = new PlatformCode("vector");

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final DeleteSoftwareHandler handler = new DeleteSoftwareHandler(repository, event -> {});

    @Test
    void removesTheSoftware() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                HardwareSet.restore(VECTOR, Set.of()),
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));

        handler.handle(new DeleteSoftwareCommand(ADMIN, ID));

        assertThat(repository.findById(ID)).isEmpty();
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new DeleteSoftwareCommand(ADMIN, ID)))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void refusesSomeoneWhoMayNotChangeTheSoftwareAndKeepsIt() {
        repository.save(Software.create(
                ID,
                new SoftwareTitle("Elite"),
                null,
                null,
                HardwareSet.restore(VECTOR, Set.of()),
                CategorySet.EMPTY,
                SoftwareCredits.EMPTY,
                BY_OTHER));

        assertThatThrownBy(() -> handler.handle(new DeleteSoftwareCommand(MEMBER, ID)))
                .isInstanceOf(ChangeForbiddenException.class);
        assertThat(repository.findById(ID)).isPresent();
    }
}
