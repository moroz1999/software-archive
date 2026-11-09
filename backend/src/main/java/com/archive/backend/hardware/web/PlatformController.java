package com.archive.backend.hardware.web;

import com.archive.backend.hardware.app.ListPlatformsHandler;
import com.archive.backend.hardware.app.ListPlatformsQuery;
import com.archive.backend.hardware.web.dto.PlatformDto;
import com.archive.backend.hardware.web.mapper.PlatformWebMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/platforms")
@RequiredArgsConstructor
public class PlatformController {

    private final ListPlatformsHandler handler;
    private final PlatformWebMapper webMapper;

    @GetMapping
    public List<PlatformDto> platforms() {
        return webMapper.toPlatforms(handler.handle(new ListPlatformsQuery()));
    }
}
