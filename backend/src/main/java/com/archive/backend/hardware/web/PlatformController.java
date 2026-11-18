package com.archive.backend.hardware.web;

import com.archive.backend.hardware.app.ListPlatformsHandler;
import com.archive.backend.hardware.app.ListPlatformsQuery;
import com.archive.backend.hardware.web.dto.PlatformDto;
import com.archive.backend.hardware.web.mapper.PlatformWebMapper;
import com.archive.backend.shared.domain.Language;
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
    public List<PlatformDto> platforms(Language language) {
        return webMapper.toPlatforms(handler.handle(new ListPlatformsQuery(language)));
    }
}
