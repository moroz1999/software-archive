package com.archive.backend.software.app;

import static com.archive.backend.software.app.ReleaseFixtures.AY;
import static com.archive.backend.software.app.ReleaseFixtures.AY_MOD_CONTENT;
import static com.archive.backend.software.app.ReleaseFixtures.BK0011M;
import static com.archive.backend.software.app.ReleaseFixtures.ELITE;
import static com.archive.backend.software.app.ReleaseFixtures.elite;
import static com.archive.backend.software.app.ReleaseFixtures.hardwareSets;
import static com.archive.backend.software.app.ReleaseFixtures.vector;
import static com.archive.backend.user.TestActors.ADMIN;
import static com.archive.backend.user.TestActors.MEMBER;
import static com.archive.backend.user.TestActors.MEMBER_ID;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import com.archive.backend.shared.domain.error.NotFoundException;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.software.domain.ReleaseArchiveRepository;
import com.archive.backend.software.domain.ReleaseArchiveUpload;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.domain.AddedBy;
import com.archive.backend.user.domain.SignInRequiredException;
import java.io.ByteArrayInputStream;
import java.util.Set;
import org.junit.jupiter.api.Test;

class CreateReleaseHandlerTest {

    private final InMemorySoftwareRepository repository = new InMemorySoftwareRepository();
    private final ReleaseArchiveRepository archiveFiles = mock(ReleaseArchiveRepository.class);
    private final CreateReleaseHandler handler = new CreateReleaseHandler(repository, hardwareSets(), archiveFiles);

    @Test
    void addsTheReleaseToItsSoftwareUnderANewId() {
        repository.save(elite());

        ReleaseId id = handler.handle(new CreateReleaseCommand(ADMIN, ELITE, AY_MOD_CONTENT, Set.of(AY)));

        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(software -> assertThat(software.release(id)).hasValueSatisfying(release -> {
                    assertThat(release.content()).isEqualTo(AY_MOD_CONTENT);
                    assertThat(release.hardware()).isEqualTo(vector(AY));
                }));
    }

    @Test
    void failsWhenTheSoftwareDoesNotExist() {
        assertThatThrownBy(() -> handler.handle(new CreateReleaseCommand(ADMIN, ELITE, AY_MOD_CONTENT, Set.of(AY))))
                .isInstanceOfSatisfying(
                        NotFoundException.class, e -> assertThat(e.code()).isEqualTo("software.not-found"));
    }

    @Test
    void refusesHardwareOfAnotherPlatformAndAddsNothing() {
        repository.save(elite());

        assertThatThrownBy(
                        () -> handler.handle(new CreateReleaseCommand(ADMIN, ELITE, AY_MOD_CONTENT, Set.of(BK0011M))))
                .isInstanceOfSatisfying(RuleViolationException.class, e -> {
                    assertThat(e.code()).isEqualTo("hardware.unknown");
                    assertThat(e.field()).isEqualTo("hardware");
                });
        assertThat(repository.findById(ELITE))
                .hasValueSatisfying(
                        software -> assertThat(software.getReleases()).isEmpty());
    }

    @Test
    void remembersWhoAddedTheRelease() {
        repository.save(elite());

        ReleaseId id = handler.handle(new CreateReleaseCommand(MEMBER, ELITE, AY_MOD_CONTENT, Set.of(AY)));

        assertThat(repository.findById(ELITE).flatMap(software -> software.release(id)))
                .hasValueSatisfying(release -> assertThat(release.addedBy()).isEqualTo(new AddedBy(MEMBER_ID)));
    }

    @Test
    void refusesAPublicVisitor() {
        repository.save(elite());

        assertThatThrownBy(() ->
                        handler.handle(new CreateReleaseCommand(Actor.anonymous(), ELITE, AY_MOD_CONTENT, Set.of(AY))))
                .isInstanceOf(SignInRequiredException.class);
    }

    @Test
    void storesTheOneArchiveOfTheRelease() {
        repository.save(elite());
        ReleaseArchiveUpload upload =
                new ReleaseArchiveUpload("elite.zip", 7, () -> new ByteArrayInputStream(new byte[7]));

        ReleaseId id = handler.handle(new CreateReleaseCommand(ADMIN, ELITE, AY_MOD_CONTENT, Set.of(), upload));

        assertThat(repository.findById(ELITE).flatMap(software -> software.release(id)))
                .hasValueSatisfying(release -> assertThat(release.archive())
                        .isNotNull()
                        .satisfies(archive -> {
                            assertThat(archive.fileName()).isEqualTo("elite.zip");
                            assertThat(archive.size()).isEqualTo(7);
                            verify(archiveFiles).store(archive.id(), upload);
                        }));
    }
}
