package com.archive.backend.software.infra;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SoftwareJpaRepository extends JpaRepository<SoftwareEntity, UUID> {}
