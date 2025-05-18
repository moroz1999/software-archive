package com.archive.backend.repository;

import com.archive.backend.model.Software;
import org.springframework.data.jpa.repository.JpaRepository;


import java.util.UUID;

public interface SoftwareRepository extends JpaRepository<Software, UUID> {

}