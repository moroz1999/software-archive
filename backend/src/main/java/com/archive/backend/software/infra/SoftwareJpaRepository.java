package com.archive.backend.software.infra;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SoftwareJpaRepository extends JpaRepository<SoftwareEntity, UUID> {
}
