package com.archive.backend.software.web;

import com.archive.backend.software.app.LookupSoftwareReferencesHandler;
import com.archive.backend.software.web.dto.SoftwareReferenceLookupDto;
import com.archive.backend.software.web.mapper.SoftwareReferenceWebMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * AJAX lookup for the relation pickers of the software form.
 */
@RestController
@RequestMapping("/api/entity-lookup")
@RequiredArgsConstructor
public class SoftwareReferenceController {

    private final LookupSoftwareReferencesHandler handler;
    private final SoftwareReferenceWebMapper mapper;

    @GetMapping
    public SoftwareReferenceLookupDto lookup(
            @RequestParam(defaultValue = "") String query, @RequestParam(defaultValue = "") String types) {
        return mapper.toDto(handler.handle(mapper.toQuery(query, types)));
    }
}
