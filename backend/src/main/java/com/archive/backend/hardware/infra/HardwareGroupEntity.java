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
import java.util.Map;
import lombok.Data;

/** A group of hardware with its names by the codes of their languages and its place in the order of the groups. */
@Entity
@Table(name = "hardware_group")
@Data
public class HardwareGroupEntity {

    @Id
    private String code;

    @Column(nullable = false)
    private int position;

    @ElementCollection
    @CollectionTable(name = "hardware_group_name", joinColumns = @JoinColumn(name = "group_code"))
    @MapKeyColumn(name = "language")
    @Column(name = "name", nullable = false)
    private Map<String, String> names = new HashMap<>();
}
