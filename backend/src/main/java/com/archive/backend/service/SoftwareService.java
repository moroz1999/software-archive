package com.archive.backend.service;

import com.archive.backend.model.Software;
import com.archive.backend.repository.SoftwareRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class SoftwareService {
    private final SoftwareRepository repository;

    public SoftwareService(SoftwareRepository repository) {
        this.repository = repository;
    }

    public List<Software> getAll() {
        return repository.findAll();
    }
}