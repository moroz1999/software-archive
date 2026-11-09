package com.archive.backend.software.web;

import com.archive.backend.software.app.CreateSoftwareHandler;
import com.archive.backend.software.app.DeleteSoftwareCommand;
import com.archive.backend.software.app.DeleteSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareHandler;
import com.archive.backend.software.app.GetSoftwareQuery;
import com.archive.backend.software.app.ListLatestSoftwareHandler;
import com.archive.backend.software.app.ListSoftwareHandler;
import com.archive.backend.software.app.ListSoftwareReleasesHandler;
import com.archive.backend.software.app.ListSoftwareReleasesQuery;
import com.archive.backend.software.app.UpdateSoftwareHandler;
import com.archive.backend.software.domain.SoftwareId;
import com.archive.backend.software.web.dto.SoftwareCreatedDto;
import com.archive.backend.software.web.dto.SoftwareDetailsDto;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import com.archive.backend.software.web.dto.SoftwareReleaseDto;
import com.archive.backend.software.web.dto.SoftwareRequestDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import java.net.URI;
import java.util.List;
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
@RequestMapping("/api/software")
@RequiredArgsConstructor
public class SoftwareController {

    private final ListSoftwareHandler listHandler;
    private final ListLatestSoftwareHandler latestHandler;
    private final GetSoftwareHandler getHandler;
    private final ListSoftwareReleasesHandler releasesHandler;
    private final CreateSoftwareHandler createHandler;
    private final UpdateSoftwareHandler updateHandler;
    private final DeleteSoftwareHandler deleteHandler;
    private final SoftwareWebMapper webMapper;

    @GetMapping
    public Page<SoftwareListItemDto> list(
            @RequestParam(required = false) @Nullable String platform, Pageable pageable) {
        return listHandler.handle(webMapper.toListQuery(platform, pageable)).map(webMapper::toListItem);
    }

    /** The most recently added software; only the page size is taken from the request. */
    @GetMapping("/latest")
    public Page<SoftwareListItemDto> latest(
            @RequestParam(required = false) @Nullable String platform, Pageable pageable) {
        return latestHandler.handle(webMapper.toLatestQuery(platform, pageable)).map(webMapper::toListItem);
    }

    @GetMapping("/{id}")
    public SoftwareDetailsDto get(@PathVariable UUID id) {
        return webMapper.toDetails(getHandler.handle(new GetSoftwareQuery(new SoftwareId(id))));
    }

    /** The releases of the software, each with the hardware it shows. */
    @GetMapping("/{id}/releases")
    public List<SoftwareReleaseDto> releases(@PathVariable UUID id) {
        return webMapper.toReleases(releasesHandler.handle(new ListSoftwareReleasesQuery(new SoftwareId(id))));
    }

    @PostMapping
    public ResponseEntity<SoftwareCreatedDto> create(@RequestBody SoftwareRequestDto request) {
        SoftwareId id = createHandler.handle(webMapper.toCreate(request));
        return ResponseEntity.created(URI.create("/api/software/" + id.value())).body(webMapper.toCreated(id));
    }

    @PutMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(@PathVariable UUID id, @RequestBody SoftwareRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(id, request));
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(@PathVariable UUID id) {
        deleteHandler.handle(new DeleteSoftwareCommand(new SoftwareId(id)));
    }
}
