package com.archive.backend.software.web;

import com.archive.backend.software.app.ListSoftware;
import com.archive.backend.software.app.ListSoftwareHandler;
import com.archive.backend.software.web.dto.SoftwareListItemDto;
import com.archive.backend.software.web.mapper.SoftwareWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/software")
@RequiredArgsConstructor
public class SoftwareController {

    private final ListSoftwareHandler handler;
    private final SoftwareWebMapper webMapper;

    @GetMapping
    public Page<SoftwareListItemDto> list(Pageable pageable) {
        return handler.handle(new ListSoftware(pageable))
                .map(webMapper::toListItem);
    }
}