package com.archive.backend.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "software")
@Data
public class Software {
    @Id
    private UUID id;

    private String title;
    private String description;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column(name = "release_date")
    private LocalDateTime releaseDate;
}
