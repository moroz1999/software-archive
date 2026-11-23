package com.archive.backend.hardware.infra;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.MapKeyColumn;
import jakarta.persistence.Table;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import lombok.Data;

/** An item of hardware with its names by the codes of their languages, its group and its platforms by code. */
@Entity
@Table(name = "hardware")
@Data
public class HardwareEntity {

    @Id
    private String code;

    @Column(name = "hardware_group", nullable = false)
    private String group;

    @ElementCollection
    @CollectionTable(name = "hardware_name", joinColumns = @JoinColumn(name = "hardware_code"))
    @MapKeyColumn(name = "language")
    @Column(name = "name", nullable = false)
    private Map<String, String> names = new HashMap<>();

    @ElementCollection
    @CollectionTable(name = "hardware_platform", joinColumns = @JoinColumn(name = "hardware_code"))
    @Column(name = "platform_code", nullable = false)
    private Set<String> platforms = new HashSet<>();
}
