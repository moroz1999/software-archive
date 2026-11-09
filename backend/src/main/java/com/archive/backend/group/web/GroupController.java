package com.archive.backend.group.web;

import com.archive.backend.group.app.GetGroupHandler;
import com.archive.backend.group.app.GetGroupQuery;
import com.archive.backend.group.app.ListGroupsHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.web.dto.GroupCatalogEntryDto;
import com.archive.backend.group.web.dto.GroupDetailsDto;
import com.archive.backend.group.web.mapper.GroupWebMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final ListGroupsHandler listHandler;
    private final GetGroupHandler getHandler;
    private final GroupWebMapper webMapper;

    @GetMapping
    public Page<GroupCatalogEntryDto> list(
            @RequestParam(required = false) @Nullable String letter,
            @RequestParam(required = false) @Nullable UUID country,
            @RequestParam(required = false) @Nullable UUID city,
            @RequestParam(required = false) @Nullable String platform,
            Pageable pageable) {
        return listHandler
                .handle(webMapper.toListQuery(letter, country, city, platform, pageable))
                .map(webMapper::toCatalogEntry);
    }

    @GetMapping("/{id}")
    public GroupDetailsDto get(@PathVariable UUID id) {
        return webMapper.toDetails(getHandler.handle(new GetGroupQuery(new GroupId(id))));
    }
}
