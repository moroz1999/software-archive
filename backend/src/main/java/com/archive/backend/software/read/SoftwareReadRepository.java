package com.archive.backend.software.read;

import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Reads software as plain read models, never as domain entities (docs/backend.md).
 */
public interface SoftwareReadRepository {

    /**
     * The most recently added software first, only of the platform when one is given; the sort is ignored.
     */
    Page<SoftwareSummary> findLatest(@Nullable String platform, Pageable pageable);

    /**
     * Software with its platform, hardware and categories named in the language of the given code, or in Russian when
     * they have no name in that one.
     */
    Optional<SoftwareDetails> findById(UUID id, String language);

    /** Whether the software is in the archive. */
    boolean exists(UUID id);

    /**
     * The releases of software, each with only its own hardware, named as in {@link #findById}; none for unknown
     * software.
     */
    List<SoftwareRelease> findReleases(UUID softwareId, String language);

    /**
     * A release of the software as it states itself, its hardware named as in {@link #findById}; empty when the
     * software has no such release.
     */
    Optional<ReleaseDetails> findRelease(UUID softwareId, UUID releaseId, String language);

    /** Whether the software has such a release. */
    boolean releaseExists(UUID softwareId, UUID releaseId);

    /**
     * The software the person is credited on, by themselves or by a label, in the order of the release dates and then
     * of the titles; none for an unknown person.
     */
    List<CreditedSoftware> findCreditedOnPerson(UUID personId);

    /** The same for a group. */
    List<CreditedSoftware> findCreditedOnGroup(UUID groupId);

    /** The software a label is credited on, by itself. */
    List<CreditedSoftware> findCreditedOnLabel(UUID labelId);

    /**
     * The releases the person is credited on, by themselves or by a label, in the order of the release dates and then
     * of the titles; none for an unknown person.
     */
    List<CreditedRelease> findReleasesCreditedOnPerson(UUID personId);

    /** The same for a group. */
    List<CreditedRelease> findReleasesCreditedOnGroup(UUID groupId);

    /** The releases a label is credited on, by itself. */
    List<CreditedRelease> findReleasesCreditedOnLabel(UUID labelId);

    /** The software's own screenshots in their order; none for unknown software. */
    List<SoftwareScreenshot> findScreenshots(UUID softwareId);

    /** The pictures of a gallery of the software, by the gallery's code, in their order; none for unknown software. */
    List<SoftwareScreenshot> findPictures(UUID softwareId, String gallery);

    /** Everyone credited on a release, as its page lists them. */
    List<SoftwareCredit> findReleaseCredits(UUID releaseId);

    /** The screenshots of a release in their order; none for an unknown release. */
    List<SoftwareScreenshot> findReleaseScreenshots(UUID releaseId);
}
