package com.archive.backend.software.web;

import com.archive.backend.software.app.CreateReleaseHandler;
import com.archive.backend.software.app.DeleteReleaseHandler;
import com.archive.backend.software.app.GetReleaseArchiveFileHandler;
import com.archive.backend.software.app.GetReleaseArchiveFileQuery;
import com.archive.backend.software.app.GetSoftwareReleaseHandler;
import com.archive.backend.software.app.UpdateReleaseHandler;
import com.archive.backend.software.domain.ReleaseId;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.read.ReleaseArchiveFile;
import com.archive.backend.software.web.dto.ReleaseCreatedDto;
import com.archive.backend.software.web.dto.ReleaseDetailsDto;
import com.archive.backend.software.web.dto.ReleaseRequestDto;
import com.archive.backend.software.web.mapper.ReleaseWebMapper;
import com.archive.backend.user.domain.Actor;
import java.net.URI;
import java.nio.charset.StandardCharsets;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.ContentDisposition;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

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
    private final GetReleaseArchiveFileHandler archiveFileHandler;
    private final ReleaseWebMapper webMapper;

    /**
     * A release as it states itself, to edit it.
     */
    @GetMapping("/{releaseId}")
    public ReleaseDetailsDto get(@PathVariable UUID softwareId, @PathVariable UUID releaseId) {
        return webMapper.toDetails(softwareId, getHandler.handle(webMapper.toGet(softwareId, releaseId)));
    }

    @PostMapping(consumes = MediaType.APPLICATION_JSON_VALUE)
    public ResponseEntity<ReleaseCreatedDto> create(
            Actor actor, @PathVariable UUID softwareId, @RequestBody ReleaseRequestDto request) {
        ReleaseId id = createHandler.handle(webMapper.toCreate(actor, softwareId, request));
        return ResponseEntity.created(URI.create("/api/software/" + softwareId + "/releases/" + id.value()))
                .body(webMapper.toCreated(id));
    }

    @PostMapping(consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ReleaseCreatedDto> createWithArchive(
            Actor actor,
            @PathVariable UUID softwareId,
            @RequestPart("release") ReleaseRequestDto request,
            @RequestPart(value = "archive", required = false) @Nullable MultipartFile archive) {
        ReleaseId id = createHandler.handle(webMapper.toCreate(actor, softwareId, request, archive));
        return ResponseEntity.created(URI.create("/api/software/" + softwareId + "/releases/" + id.value()))
                .body(webMapper.toCreated(id));
    }

    @PutMapping(path = "/{releaseId}", consumes = MediaType.APPLICATION_JSON_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(
            Actor actor,
            @PathVariable UUID softwareId,
            @PathVariable UUID releaseId,
            @RequestBody ReleaseRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(actor, softwareId, releaseId, request));
    }

    @PutMapping(path = "/{releaseId}", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void updateWithArchive(
            Actor actor,
            @PathVariable UUID softwareId,
            @PathVariable UUID releaseId,
            @RequestPart("release") ReleaseRequestDto request,
            @RequestPart(value = "archive", required = false) @Nullable MultipartFile archive,
            @RequestParam(defaultValue = "false") boolean removeArchive) {
        updateHandler.handle(webMapper.toUpdate(actor, softwareId, releaseId, request, archive, removeArchive));
    }

    @GetMapping("/{releaseId}/archive")
    public ResponseEntity<Resource> archive(@PathVariable UUID softwareId, @PathVariable UUID releaseId) {
        ReleaseArchiveFile file = archiveFileHandler.handle(
                new GetReleaseArchiveFileQuery(new SoftwareId(softwareId), new ReleaseId(releaseId)));
        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(file.fileName(), StandardCharsets.UTF_8)
                .build();
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_OCTET_STREAM)
                .contentLength(file.size())
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .body(new FileSystemResource(file.path()));
    }

    @DeleteMapping("/{releaseId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Actor actor, @PathVariable UUID softwareId, @PathVariable UUID releaseId) {
        deleteHandler.handle(webMapper.toDelete(actor, softwareId, releaseId));
    }
}
