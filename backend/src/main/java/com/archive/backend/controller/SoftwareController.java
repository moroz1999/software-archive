package com.archive.backend.controller;

import com.archive.backend.model.Software;
import com.archive.backend.service.SoftwareService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/software")
public class SoftwareController {
    private final SoftwareService softwareService;

    public SoftwareController(SoftwareService softwareService) {
        this.softwareService = softwareService;
    }

    @GetMapping
    public List<Software> getAllSoftware() {
        return softwareService.getAll();
    }
}
