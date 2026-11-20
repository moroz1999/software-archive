package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.IntegrationTest;
import com.archive.backend.group.domain.GroupTypeCode;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

/** Checks types against the reference book the schema brings. */
@IntegrationTest
@Transactional
class JdbcGroupTypeCatalogIT {

    @Autowired
    private JdbcGroupTypeCatalog catalog;

    @Test
    void knowsTheTypesOfTheReferenceBook() {
        assertThat(catalog.allExist(Set.of(new GroupTypeCode("demogroup"), new GroupTypeCode("crack group"))))
                .isTrue();
    }

    @Test
    void knowsNoOtherType() {
        assertThat(catalog.allExist(Set.of(new GroupTypeCode("demogroup"), new GroupTypeCode("orchestra"))))
                .isFalse();
    }

    @Test
    void hasNothingMissingOfNoTypes() {
        assertThat(catalog.allExist(Set.of())).isTrue();
    }
}
