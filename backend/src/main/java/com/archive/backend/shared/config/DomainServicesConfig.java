package com.archive.backend.shared.config;

import com.archive.backend.author.domain.PersonReferenceCatalog;
import com.archive.backend.category.domain.CategoryCatalog;
import com.archive.backend.category.domain.CategoryHierarchy;
import com.archive.backend.category.domain.CategoryParents;
import com.archive.backend.category.domain.CategorySets;
import com.archive.backend.geo.domain.GeoCatalog;
import com.archive.backend.geo.domain.Places;
import com.archive.backend.group.domain.GroupMemberSets;
import com.archive.backend.group.domain.GroupTypeCatalog;
import com.archive.backend.group.domain.GroupTypes;
import com.archive.backend.hardware.domain.HardwareCatalog;
import com.archive.backend.hardware.domain.HardwareSets;
import com.archive.backend.software.domain.SoftwareCreditSets;
import com.archive.backend.software.domain.SoftwareReferenceCatalog;
import com.archive.backend.software.domain.SoftwareReferenceSets;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Domain services are plain Java (docs/backend.md); Spring gets them here.
 */
@Configuration
public class DomainServicesConfig {

    @Bean
    public CategoryParents categoryParents(CategoryHierarchy hierarchy) {
        return new CategoryParents(hierarchy);
    }

    @Bean
    public CategorySets categorySets(CategoryCatalog catalog) {
        return new CategorySets(catalog);
    }

    @Bean
    public GroupTypes groupTypes(GroupTypeCatalog catalog) {
        return new GroupTypes(catalog);
    }

    @Bean
    public GroupMemberSets groupMemberSets(PersonReferenceCatalog catalog) {
        return new GroupMemberSets(catalog);
    }

    @Bean
    public HardwareSets hardwareSets(HardwareCatalog catalog) {
        return new HardwareSets(catalog);
    }

    @Bean
    public Places places(GeoCatalog catalog) {
        return new Places(catalog);
    }

    @Bean
    public SoftwareReferenceSets softwareReferenceSets(SoftwareReferenceCatalog catalog) {
        return new SoftwareReferenceSets(catalog);
    }

    @Bean
    public SoftwareCreditSets softwareCreditSets(PersonReferenceCatalog catalog) {
        return new SoftwareCreditSets(catalog);
    }
}
