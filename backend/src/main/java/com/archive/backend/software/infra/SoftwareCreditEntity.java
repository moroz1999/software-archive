package com.archive.backend.software.infra;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.EqualsAndHashCode;

@Entity
@Table(name = "software_contribution")
@EqualsAndHashCode(callSuper = true)
public class SoftwareCreditEntity extends CreditColumns {}
