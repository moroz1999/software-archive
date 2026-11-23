package com.archive.backend.software.web;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.label.domain.LabelId;
import com.archive.backend.software.app.ListAuthorReleasesQuery;
import com.archive.backend.software.app.ListAuthorSoftwareQuery;
import com.archive.backend.software.app.ListCreditedReleasesHandler;
import com.archive.backend.software.app.ListCreditedSoftwareHandler;
import com.archive.backend.software.app.ListGroupReleasesQuery;
import com.archive.backend.software.app.ListGroupSoftwareQuery;
import com.archive.backend.software.app.ListLabelReleasesQuery;
import com.archive.backend.software.app.ListLabelSoftwareQuery;
import com.archive.backend.software.web.dto.CreditedReleaseDto;
import com.archive.backend.software.web.dto.CreditedSoftwareDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/**
 * The software and the releases on the pages of people, groups and labels: whatever they are credited on
 * (docs/features/author-view.md).
 */
@RestController
@RequiredArgsConstructor
public class CreditedSoftwareController {

    private final ListCreditedSoftwareHandler handler;
    private final ListCreditedReleasesHandler releasesHandler;
    private final SoftwareWebMapper webMapper;

    @GetMapping("/api/authors/{id}/software")
    public List<CreditedSoftwareDto> ofAuthor(@PathVariable UUID id) {
        return webMapper.toCredited(handler.handle(new ListAuthorSoftwareQuery(new AuthorId(id))));
    }

    @GetMapping("/api/labels/{id}/software")
    public List<CreditedSoftwareDto> ofLabel(@PathVariable UUID id) {
        return webMapper.toCredited(handler.handle(new ListLabelSoftwareQuery(new LabelId(id))));
    }

    @GetMapping("/api/groups/{id}/software")
    public List<CreditedSoftwareDto> ofGroup(@PathVariable UUID id) {
        return webMapper.toCredited(handler.handle(new ListGroupSoftwareQuery(new GroupId(id))));
    }

    @GetMapping("/api/authors/{id}/releases")
    public List<CreditedReleaseDto> releasesOfAuthor(@PathVariable UUID id) {
        return webMapper.toCreditedReleases(releasesHandler.handle(new ListAuthorReleasesQuery(new AuthorId(id))));
    }

    @GetMapping("/api/labels/{id}/releases")
    public List<CreditedReleaseDto> releasesOfLabel(@PathVariable UUID id) {
        return webMapper.toCreditedReleases(releasesHandler.handle(new ListLabelReleasesQuery(new LabelId(id))));
    }

    @GetMapping("/api/groups/{id}/releases")
    public List<CreditedReleaseDto> releasesOfGroup(@PathVariable UUID id) {
        return webMapper.toCreditedReleases(releasesHandler.handle(new ListGroupReleasesQuery(new GroupId(id))));
    }
}
