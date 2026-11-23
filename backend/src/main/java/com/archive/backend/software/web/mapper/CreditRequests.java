package com.archive.backend.software.web.mapper;

import com.archive.backend.shared.domain.error.InvalidValueException;
import com.archive.backend.software.domain.SoftwareCreditDraft;
import com.archive.backend.software.domain.SoftwareReference;
import com.archive.backend.software.domain.SoftwareReferenceId;
import com.archive.backend.software.domain.SoftwareReferenceType;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import java.util.List;
import java.util.UUID;
import org.jspecify.annotations.Nullable;

/** The credits sent with software or a release, as the domain takes them. */
final class CreditRequests {

    private CreditRequests() {}

    static List<SoftwareCreditDraft> toCredits(@Nullable List<SoftwareRequestDto.CreditDto> credits) {
        return credits == null
                ? List.of()
                : credits.stream().map(CreditRequests::toCredit).toList();
    }

    private static SoftwareCreditDraft toCredit(SoftwareRequestDto.CreditDto credit) {
        UUID id = credit.id();
        if (id == null) {
            throw new InvalidValueException("software.reference-id-required", "A credited id is required.");
        }
        String type = credit.type();
        if (type == null) {
            throw new InvalidValueException("software.reference-type-invalid", "A credited type is required.");
        }
        return new SoftwareCreditDraft(
                new SoftwareReference(SoftwareReferenceType.fromCode(type), new SoftwareReferenceId(id)),
                credit.roles() == null ? List.of() : credit.roles());
    }
}
