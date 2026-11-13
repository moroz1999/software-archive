package com.archive.backend.importer.infra;

import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataExternalOriginRepository extends JpaRepository<ExternalOriginEntity, ExternalOriginKey> {}
