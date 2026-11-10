package com.archive.backend.shared.config;

import com.archive.backend.hardware.domain.HardwareCatalog;
import com.archive.backend.hardware.domain.HardwareSets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain services are plain Java (docs/backend.md); Spring gets them here.
 */
@Configuration
public class DomainServicesConfig {

    @Bean
    public HardwareSets hardwareSets(HardwareCatalog catalog) {
        return new HardwareSets(catalog);
    }
}
