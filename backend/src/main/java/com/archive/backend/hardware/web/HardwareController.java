package com.archive.backend.hardware.web;

import com.archive.backend.hardware.app.ListHardwareHandler;
import com.archive.backend.hardware.web.dto.HardwareDto;
import com.archive.backend.hardware.web.mapper.HardwareWebMapper;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.jspecify.annotations.Nullable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/hardware")
@RequiredArgsConstructor
public class HardwareController {

    private final ListHardwareHandler handler;
    private final HardwareWebMapper webMapper;

    /**
     * The hardware of a platform, to choose from.
     */
    @GetMapping
    public List<HardwareDto> list(@RequestParam(required = false) @Nullable String platform) {
        return webMapper.toDtos(handler.handle(webMapper.toQuery(platform)));
    }
}
