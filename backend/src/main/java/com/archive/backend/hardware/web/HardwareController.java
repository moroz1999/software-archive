package com.archive.backend.hardware.web;

import com.archive.backend.hardware.app.CreateHardwareHandler;
import com.archive.backend.hardware.app.DeleteHardwareCommand;
import com.archive.backend.hardware.app.DeleteHardwareHandler;
import com.archive.backend.hardware.app.GetHardwareHandler;
import com.archive.backend.hardware.app.GetHardwareQuery;
import com.archive.backend.hardware.app.ListHardwareHandler;
import com.archive.backend.hardware.app.UpdateHardwareHandler;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.web.dto.HardwareCodeDto;
import com.archive.backend.hardware.web.dto.HardwareDetailsDto;
import com.archive.backend.hardware.web.dto.HardwareDto;
import com.archive.backend.hardware.web.dto.HardwareRequestDto;
import com.archive.backend.hardware.web.mapper.HardwareWebMapper;
import com.archive.backend.shared.domain.Language;
import com.archive.backend.user.domain.Actor;
import java.net.URI;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
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

/** The hardware reference book: to choose from, and kept from the management (docs/features/hardware-editing.md). */
@RestController
@RequestMapping("/api/hardware")
@RequiredArgsConstructor
public class HardwareController {

    private final ListHardwareHandler handler;
    private final GetHardwareHandler getHandler;
    private final CreateHardwareHandler createHandler;
    private final UpdateHardwareHandler updateHandler;
    private final DeleteHardwareHandler deleteHandler;
    private final HardwareWebMapper webMapper;

    /**
     * The hardware of a platform, to choose from.
     */
    @GetMapping
    public List<HardwareDto> list(@RequestParam(required = false) @Nullable String platform, Language language) {
        return webMapper.toDtos(handler.handle(webMapper.toQuery(platform, language)));
    }

    @GetMapping("/{code}")
    public HardwareDetailsDto hardware(@PathVariable String code) {
        return webMapper.toDto(getHandler.handle(new GetHardwareQuery(new HardwareCode(code))));
    }

    @ResponseStatus(HttpStatus.CREATED)
    @PostMapping
    public ResponseEntity<HardwareCodeDto> create(Actor actor, @RequestBody HardwareRequestDto request) {
        String code = createHandler.handle(webMapper.toCreate(actor, request)).value();
        return ResponseEntity.created(URI.create("/api/hardware/" + code)).body(new HardwareCodeDto(code));
    }

    @PutMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void update(Actor actor, @PathVariable String code, @RequestBody HardwareRequestDto request) {
        updateHandler.handle(webMapper.toUpdate(actor, code, request));
    }

    @DeleteMapping("/{code}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void delete(Actor actor, @PathVariable String code) {
        deleteHandler.handle(new DeleteHardwareCommand(actor, new HardwareCode(code)));
    }
}
