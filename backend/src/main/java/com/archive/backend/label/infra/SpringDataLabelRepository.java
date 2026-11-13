package com.archive.backend.label.infra;

import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SpringDataLabelRepository extends JpaRepository<LabelEntity, UUID> {}
