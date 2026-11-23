package com.archive.backend.hardware.web;

import com.archive.backend.hardware.app.CreateHardwareGroupHandler;
import com.archive.backend.hardware.app.DeleteHardwareGroupCommand;
import com.archive.backend.hardware.app.DeleteHardwareGroupHandler;
import com.archive.backend.hardware.app.GetHardwareGroupHandler;
import com.archive.backend.hardware.app.GetHardwareGroupQuery;
import com.archive.backend.hardware.app.ListHardwareGroupsHandler;
import com.archive.backend.hardware.app.ListHardwareGroupsQuery;
import com.archive.backend.hardware.app.UpdateHardwareGroupHandler;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.web.dto.HardwareCodeDto;
import com.archive.backend.hardware.web.dto.HardwareGroupDetailsDto;
import com.archive.backend.hardware.web.dto.HardwareGroupDto;
import com.archive.backend.hardware.web.dto.HardwareGroupRequestDto;
import com.archive.backend.hardware.web.mapper.HardwareWebMapper;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/** The groups of hardware kept from the management (docs/features/hardware-editing.md). */
@RestController
@RequestMapping("/api/hardware-groups")
@RequiredArgsConstructor
public class HardwareGroupController {

    private final ListHardwareGroupsHandler listHandler;
    private final GetHardwareGroupHandler getHandler;
    private final CreateHardwareGroupHandler createHandler;
    private final UpdateHardwareGroupHandler updateHandler;
    private final DeleteHardwareGroupHandler deleteHandler;
    private final HardwareWebMapper webMapper;

    /** Every group with its hardware at once: the list of the management needs all of it. */
    @GetMapping
    public List<HardwareGroupDto> groups(Language language) {
        return webMapper.toGroupDtos(listHandler.handle(new ListHardwareGroupsQuery(language)));
    }

    @GetMapping("/{code}")
    public HardwareGroupDetailsDto group(@PathVariable String code) {
        return webMapper.toDto(getHandler.handle(new GetHardwareGroupQuery(new HardwareGroupCode(code))));
    }

    @PostMapping
    public ResponseEntity<HardwareCodeDto> create(Actor actor, @RequestBody HardwareGroupRequestDto request) {
        String code =
                createHandler.handle(webMapper.toCreateGroup(actor, request)).value();
        return ResponseEntity.created(URI.create("/api/hardware-groups/" + code))
                .body(new HardwareCodeDto(code));
    }

    @PutMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(Actor actor, @PathVariable String code, @RequestBody HardwareGroupRequestDto request) {
        updateHandler.handle(webMapper.toUpdateGroup(actor, code, request));
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Actor actor, @PathVariable String code) {
        deleteHandler.handle(new DeleteHardwareGroupCommand(actor, new HardwareGroupCode(code)));
    }
}
