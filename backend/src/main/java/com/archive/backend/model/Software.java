package com.archive.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "software")
public class Software {
    @Id
    private UUID id;

    private String name;
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
}
