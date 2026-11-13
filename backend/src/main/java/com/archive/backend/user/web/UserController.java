package com.archive.backend.user.web;

import com.archive.backend.user.app.ChangeUserGroupsHandler;
import com.archive.backend.user.app.ListUsersHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.web.dto.UserEntryDto;
import com.archive.backend.user.web.dto.UserGroupsRequestDto;
import com.archive.backend.user.web.mapper.UserManagementWebMapper;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * The users of the archive, as an administrator keeps them.
 */
@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final ListUsersHandler listHandler;
    private final ChangeUserGroupsHandler changeGroupsHandler;
    private final UserManagementWebMapper webMapper;

    @GetMapping
    public Page<UserEntryDto> list(
            Actor actor, @RequestParam(required = false) @Nullable String query, Pageable pageable) {
        return listHandler.handle(webMapper.toListQuery(actor, query, pageable)).map(webMapper::toDto);
    }

    @PutMapping("/{id}/groups")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void changeGroups(Actor actor, @PathVariable UUID id, @RequestBody UserGroupsRequestDto request) {
        changeGroupsHandler.handle(webMapper.toChangeGroups(actor, id, request));
    }
}
