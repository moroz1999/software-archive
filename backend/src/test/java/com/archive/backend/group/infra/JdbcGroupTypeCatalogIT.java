package com.archive.backend.group.infra;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.TestcontainersConfiguration;
import com.archive.backend.group.domain.GroupTypeCode;
import java.util.Set;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.JdbcTest;
import org.springframework.context.annotation.Import;

/** Checks types against the reference book the schema brings. */
@JdbcTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@Import({TestcontainersConfiguration.class, JdbcGroupTypeCatalog.class})
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
}
