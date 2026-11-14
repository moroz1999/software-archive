package com.archive.backend.software.web;

import com.archive.backend.author.domain.AuthorId;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.software.app.ListAuthorSoftwareQuery;
import com.archive.backend.software.app.ListCreditedSoftwareHandler;
import com.archive.backend.software.app.ListGroupSoftwareQuery;
import com.archive.backend.software.web.dto.CreditedSoftwareDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;

/** The software on the pages of people and groups: whatever they are credited on (docs/features/author-view.md). */
@RestController
@RequiredArgsConstructor
public class CreditedSoftwareController {

    private final ListCreditedSoftwareHandler handler;
    private final SoftwareWebMapper webMapper;

    @GetMapping("/api/authors/{id}/software")
    public List<CreditedSoftwareDto> ofAuthor(@PathVariable UUID id) {
        return webMapper.toCredited(handler.handle(new ListAuthorSoftwareQuery(new AuthorId(id))));
    }

    @GetMapping("/api/groups/{id}/software")
    public List<CreditedSoftwareDto> ofGroup(@PathVariable UUID id) {
        return webMapper.toCredited(handler.handle(new ListGroupSoftwareQuery(new GroupId(id))));
    }
}
