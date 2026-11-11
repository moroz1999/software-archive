package com.archive.backend.group.web;

import com.archive.backend.group.app.CreateGroupHandler;
import com.archive.backend.group.app.DeleteGroupCommand;
import com.archive.backend.group.app.DeleteGroupHandler;
import com.archive.backend.group.app.GetGroupHandler;
import com.archive.backend.group.app.GetGroupQuery;
import com.archive.backend.group.app.ListGroupsHandler;
import com.archive.backend.group.app.UpdateGroupHandler;
import com.archive.backend.group.domain.GroupId;
import com.archive.backend.group.web.dto.GroupCatalogEntryDto;
import com.archive.backend.group.web.dto.GroupCreatedDto;
import com.archive.backend.group.web.dto.GroupDetailsDto;
import com.archive.backend.group.web.dto.GroupRequestDto;
import com.archive.backend.group.web.mapper.GroupWebMapper;
import java.net.URI;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/groups")
@RequiredArgsConstructor
public class GroupController {

    private final ListGroupsHandler listHandler;
    private final GetGroupHandler getHandler;
    private final CreateGroupHandler createHandler;
    private final UpdateGroupHandler updateHandler;
    private final DeleteGroupHandler deleteHandler;
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

    @PostMapping
    public ResponseEntity<GroupCreatedDto> create(@RequestBody GroupRequestDto request) {
        GroupId id = createHandler.handle(webMapper.toCreate(request));
        return ResponseEntity.created(URI.create("/api/groups/" + id.value())).body(webMapper.toCreated(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @RequestBody GroupRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(id, request));
    }

    /**
     * Deletes the group with its memberships; its labels stay in the archive as labels of nobody.
     */
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteHandler.handle(new DeleteGroupCommand(new GroupId(id)));
    }
}
