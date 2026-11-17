package com.archive.backend.user.web;

import com.archive.backend.user.app.EditUserHandler;
import com.archive.backend.user.app.GetUserHandler;
import com.archive.backend.user.app.ListUsersHandler;
import com.archive.backend.user.domain.Actor;
import com.archive.backend.user.web.dto.UserEntryDto;
import com.archive.backend.user.web.dto.UserRequestDto;
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
    private final GetUserHandler getHandler;
    private final EditUserHandler editHandler;
    private final UserManagementWebMapper webMapper;

    @GetMapping
    public Page<UserEntryDto> list(
            Actor actor, @RequestParam(required = false) @Nullable String query, Pageable pageable) {
        return listHandler.handle(webMapper.toListQuery(actor, query, pageable)).map(webMapper::toDto);
    }

    @GetMapping("/{id}")
    public UserEntryDto get(Actor actor, @PathVariable UUID id) {
        return webMapper.toDto(getHandler.handle(webMapper.toGetQuery(actor, id)));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void edit(Actor actor, @PathVariable UUID id, @RequestBody UserRequestDto request) {
        editHandler.handle(webMapper.toEdit(actor, id, request));
    }
}
