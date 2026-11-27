package com.archive.backend.software.web;

import com.archive.backend.software.app.AddScreenshotsHandler;
import com.archive.backend.software.app.DeleteScreenshotHandler;
import com.archive.backend.software.app.GetScreenshotFileHandler;
import com.archive.backend.software.app.GetScreenshotFileQuery;
import com.archive.backend.software.app.ListScreenshotsHandler;
import com.archive.backend.software.app.ReorderScreenshotsHandler;
import com.archive.backend.software.domain.Gallery;
import com.archive.backend.software.domain.ScreenshotId;
import com.archive.backend.software.read.ScreenshotFile;
import com.archive.backend.software.web.dto.ScreenshotDto;
import com.archive.backend.software.web.dto.ScreenshotOrderDto;
import com.archive.backend.software.web.mapper.ScreenshotWebMapper;
import com.archive.backend.user.domain.Actor;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import java.time.Duration;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.FileSystemResource;
import org.springframework.core.io.Resource;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

/**
 * The screenshots of software and of its releases, and their pictures (docs/features/screenshots.md). Each path of
 * the software has one of its release beside it: a release names itself after the software.
 */
@RestController
@RequiredArgsConstructor
public class ScreenshotController {

    private static final String OF_SOFTWARE = "/api/software/{softwareId}/screenshots";
    private static final String OF_RELEASE = "/api/software/{softwareId}/releases/{releaseId}/screenshots";
    /** The other galleries of the software, each beside its screenshots; a release has screenshots only. */
    private static final String OF_GALLERY = "/api/software/{softwareId}/{gallery:maps|covers|adverts}";

    /** A picture never changes under its id: a new one is a new screenshot. */
    private static final CacheControl FOREVER =
            CacheControl.maxAge(Duration.ofDays(365)).cachePublic().immutable();

    private final ListScreenshotsHandler listHandler;
    private final AddScreenshotsHandler addHandler;
    private final DeleteScreenshotHandler deleteHandler;
    private final ReorderScreenshotsHandler reorderHandler;
    private final GetScreenshotFileHandler fileHandler;
    private final ScreenshotWebMapper webMapper;

    @GetMapping(OF_SOFTWARE)
    public List<ScreenshotDto> list(@PathVariable UUID softwareId) {
        return webMapper.toDtos(listHandler.handle(webMapper.toList(softwareId, null)));
    }

    @GetMapping(OF_RELEASE)
    public List<ScreenshotDto> listOfRelease(@PathVariable UUID softwareId, @PathVariable UUID releaseId) {
        return webMapper.toDtos(listHandler.handle(webMapper.toList(softwareId, releaseId)));
    }

    @PostMapping(path = OF_SOFTWARE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ScreenshotDto> add(
            Actor actor, @PathVariable UUID softwareId, @RequestPart("files") List<MultipartFile> files) {
        return webMapper.toAdded(
                addHandler.handle(webMapper.toAdd(actor, softwareId, null, files, Gallery.SCREENSHOTS)));
    }

    @PostMapping(path = OF_RELEASE, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ScreenshotDto> addToRelease(
            Actor actor,
            @PathVariable UUID softwareId,
            @PathVariable UUID releaseId,
            @RequestPart("files") List<MultipartFile> files) {
        return webMapper.toAdded(
                addHandler.handle(webMapper.toAdd(actor, softwareId, releaseId, files, Gallery.SCREENSHOTS)));
    }

    @PutMapping(OF_SOFTWARE + "/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorder(Actor actor, @PathVariable UUID softwareId, @RequestBody ScreenshotOrderDto order) {
        reorderHandler.handle(webMapper.toReorder(actor, softwareId, null, order, Gallery.SCREENSHOTS));
    }

    @PutMapping(OF_RELEASE + "/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderOfRelease(
            Actor actor,
            @PathVariable UUID softwareId,
            @PathVariable UUID releaseId,
            @RequestBody ScreenshotOrderDto order) {
        reorderHandler.handle(webMapper.toReorder(actor, softwareId, releaseId, order, Gallery.SCREENSHOTS));
    }

    @DeleteMapping(OF_SOFTWARE + "/{screenshotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Actor actor, @PathVariable UUID softwareId, @PathVariable UUID screenshotId) {
        deleteHandler.handle(webMapper.toDelete(actor, softwareId, null, screenshotId, Gallery.SCREENSHOTS));
    }

    @DeleteMapping(OF_RELEASE + "/{screenshotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOfRelease(
            Actor actor, @PathVariable UUID softwareId, @PathVariable UUID releaseId, @PathVariable UUID screenshotId) {
        deleteHandler.handle(webMapper.toDelete(actor, softwareId, releaseId, screenshotId, Gallery.SCREENSHOTS));
    }

    @GetMapping(OF_GALLERY)
    public List<ScreenshotDto> listOfGallery(@PathVariable UUID softwareId, @PathVariable String gallery) {
        return webMapper.toDtos(listHandler.handle(webMapper.toList(softwareId, null, Gallery.fromCode(gallery))));
    }

    @PostMapping(path = OF_GALLERY, consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    @ResponseStatus(HttpStatus.CREATED)
    public List<ScreenshotDto> addToGallery(
            Actor actor,
            @PathVariable UUID softwareId,
            @PathVariable String gallery,
            @RequestPart("files") List<MultipartFile> files) {
        return webMapper.toAdded(
                addHandler.handle(webMapper.toAdd(actor, softwareId, null, files, Gallery.fromCode(gallery))));
    }

    @PutMapping(OF_GALLERY + "/order")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reorderOfGallery(
            Actor actor,
            @PathVariable UUID softwareId,
            @PathVariable String gallery,
            @RequestBody ScreenshotOrderDto order) {
        reorderHandler.handle(webMapper.toReorder(actor, softwareId, null, order, Gallery.fromCode(gallery)));
    }

    @DeleteMapping(OF_GALLERY + "/{screenshotId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteOfGallery(
            Actor actor, @PathVariable UUID softwareId, @PathVariable String gallery, @PathVariable UUID screenshotId) {
        deleteHandler.handle(webMapper.toDelete(actor, softwareId, null, screenshotId, Gallery.fromCode(gallery)));
    }

    /** The picture as it was uploaded. */
    @GetMapping("/api/screenshots/{id}")
    @ApiResponse(
            responseCode = "200",
            description = "The picture as it was uploaded",
            content = @Content(mediaType = "image/*", schema = @Schema(type = "string", format = "binary")))
    public ResponseEntity<Resource> picture(@PathVariable UUID id) {
        ScreenshotFile file = fileHandler.handle(new GetScreenshotFileQuery(new ScreenshotId(id)));
        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(file.mediaType()))
                .cacheControl(FOREVER)
                .body(new FileSystemResource(file.path()));
    }
}
