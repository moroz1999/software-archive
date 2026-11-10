package com.archive.backend.software.web;

import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.DeleteReleaseHandler;
import com.archive.backend.software.app.GetSoftwareReleaseHandler;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.web.dto.ReleaseCreatedDto;
import com.archive.backend.software.web.dto.ReleaseDetailsDto;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import java.net.URI;
import java.util.UUID;
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

/**
 * Adding, editing and deleting the releases of software; they live inside it (docs/domain/software.md).
 */
@RestController
@RequestMapping("/api/software/{softwareId}/releases")
@RequiredArgsConstructor
public class SoftwareReleaseController {

    private final GetSoftwareReleaseHandler getHandler;
    private final CreateReleaseHandler createHandler;
    private final UpdateReleaseHandler updateHandler;
    private final DeleteReleaseHandler deleteHandler;
    private final ReleaseWebMapper webMapper;

    /**
     * A release as it states itself, to edit it.
     */
    @GetMapping("/{releaseId}")
    public ReleaseDetailsDto get(@PathVariable UUID softwareId, @PathVariable UUID releaseId) {
        return webMapper.toDetails(getHandler.handle(webMapper.toGet(softwareId, releaseId)));
    }

    @PostMapping
    public ResponseEntity<ReleaseCreatedDto> create(
            @PathVariable UUID softwareId, @RequestBody ReleaseRequestDto request) {
        ReleaseId id = createHandler.handle(webMapper.toCreate(softwareId, request));
        return ResponseEntity.created(URI.create("/api/software/" + softwareId + "/releases/" + id.value()))
                .body(webMapper.toCreated(id));
    }

    @PutMapping("/{releaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            @PathVariable UUID softwareId, @PathVariable UUID releaseId, @RequestBody ReleaseRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(softwareId, releaseId, request));
    }

    @DeleteMapping("/{releaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID softwareId, @PathVariable UUID releaseId) {
        deleteHandler.handle(webMapper.toDelete(softwareId, releaseId));
    }
}
