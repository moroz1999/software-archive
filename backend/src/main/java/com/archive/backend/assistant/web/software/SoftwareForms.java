package com.archive.backend.assistant.web.software;

import com.archive.backend.assistant.web.tool.ToolContext;
import com.archive.backend.shared.domain.error.RuleViolationException;
import com.archive.backend.software.app.GetSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareQuery;
import com.archive.backend.software.app.GetSoftwareReleaseHandler;
import com.archive.backend.software.app.GetSoftwareReleaseQuery;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseDetails;
import com.archive.backend.software.read.SoftwareCategory;
import com.archive.backend.software.read.SoftwareCredit;
import com.archive.backend.software.read.SoftwareDetails;
import com.archive.backend.software.read.SoftwareHardware;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import com.archive.backend.software.web.dto.SoftwareRequestDto.CreditDto;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.stereotype.Component;

/**
 * Software and releases as their forms have them: read as they are, and sent back whole, as the forms send them, with
 * the fields a tool changes.
 */
@Component
@RequiredArgsConstructor
class SoftwareForms {

    private final GetSoftwareHandler software;
    private final GetSoftwareReleaseHandler release;

    SoftwareState software(ToolContext context, UUID id) {
        SoftwareDetails details =
                software.handle(new GetSoftwareQuery(context.actor(), new SoftwareId(id), context.language()));
        return new SoftwareState(
                details.title(),
                details.description(),
                details.releaseDate(),
                details.platform().code(),
                details.hardware().stream().map(SoftwareHardware::code).toList(),
                details.categories().stream().map(SoftwareCategory::id).toList(),
                creditStates(details.credits()),
                details.preservation());
    }

    ReleaseState release(ToolContext context, UUID softwareId, UUID releaseId) {
        ReleaseDetails details = release.handle(
                new GetSoftwareReleaseQuery(new SoftwareId(softwareId), new ReleaseId(releaseId), context.language()));
        return new ReleaseState(
                details.title(),
                details.version(),
                details.type(),
                details.releaseDate(),
                details.description(),
                details.hardware().stream().map(SoftwareHardware::code).toList(),
                creditStates(details.credits()));
    }

    /** The software as it is, with these credits. */
    static SoftwareRequestDto request(SoftwareState state, List<CreditDto> credits) {
        return new SoftwareRequestDto(
                state.title(),
                state.description(),
                state.releaseDate(),
                state.platform(),
                state.hardware(),
                state.categoryIds(),
                credits,
                state.preservation());
    }

    /** The release as it states itself, with these credits. */
    static ReleaseRequestDto request(ReleaseState state, List<CreditDto> credits) {
        return new ReleaseRequestDto(
                state.title(),
                state.version(),
                state.type(),
                state.releaseDate(),
                state.description(),
                state.hardware(),
                credits);
    }

    static @Nullable List<CreditDto> credits(@Nullable List<CreditArguments> credits) {
        return credits == null
                ? null
                : credits.stream()
                        .map(credit -> new CreditDto(credit.id(), credit.type(), credit.roles()))
                        .toList();
    }

    static List<CreditDto> currentCredits(List<CreditState> credits) {
        return credits.stream()
                .map(credit -> new CreditDto(credit.id(), credit.type(), credit.roles()))
                .toList();
    }

    /** The credits with this one put in place of the same one credited, or after the others. */
    static List<CreditDto> withCredit(List<CreditState> credits, CreditDto credit) {
        List<CreditDto> result = new ArrayList<>();
        boolean replaced = false;
        for (CreditDto current : currentCredits(credits)) {
            if (credit.id() != null && credit.id().equals(current.id())) {
                result.add(credit);
                replaced = true;
            } else {
                result.add(current);
            }
        }
        if (!replaced) {
            result.add(credit);
        }
        return result;
    }

    /** The credits without this one; refused when it is not credited. */
    static List<CreditDto> withoutCredit(List<CreditState> credits, UUID id) {
        requireCredited(credits, id);
        return currentCredits(credits).stream()
                .filter(credit -> !id.equals(credit.id()))
                .toList();
    }

    static void requireCredited(List<CreditState> credits, UUID id) {
        if (credits.stream().noneMatch(credit -> credit.id().equals(id))) {
            throw new RuleViolationException("assistant.not-credited", "Nobody of the id " + id + " is credited here.");
        }
    }

    private static List<CreditState> creditStates(List<SoftwareCredit> credits) {
        return credits.stream()
                .map(credit -> new CreditState(credit.id(), credit.type(), credit.title(), credit.roles()))
                .toList();
    }
}
