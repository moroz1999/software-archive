package com.archive.backend.importer.domain.vektor;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.category.domain.CategoryId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class VektorSectionsTest {

    @Test
    void putsASectionInItsCategory() {
        assertThat(VektorSections.placementOf("arkadnye-igry"))
                .isEqualTo(imported("8536e450-1fc7-5985-9cd2-97b499713a67"));
        assertThat(VektorSections.placementOf("zagruzchiki"))
                .isEqualTo(imported("087f5590-b140-5dda-9bd7-05d1d85b7723"));
        assertThat(VektorSections.placementOf("demo-programmy"))
                .isEqualTo(imported("c35b7f9b-44a8-5570-9406-9e6dcb30b607"));
    }

    @Test
    void leavesOutTheUtilitiesForThePc() {
        assertThat(VektorSections.placementOf("utility-dlya-pc")).isEqualTo(new VektorPlacement(false, null));
    }

    @Test
    void takesASectionItDoesNotKnowWithoutACategory() {
        assertThat(VektorSections.placementOf("novyy-razdel")).isEqualTo(new VektorPlacement(true, null));
    }

    @Test
    void knowsEverySectionOfTheSite() {
        String[] sections = {
            "3d-programmy",
            "arkadnye-igry",
            "azartnye-igry",
            "bazy-dannykh",
            "biznes-igry",
            "demo-programmy",
            "elektronnye-izdaniya",
            "emulyatory",
            "igry-na-doskakh",
            "igry-s-borboy",
            "igry-s-labirintami",
            "igry-s-pazlami",
            "igry-s-polyetami",
            "informatsionnye-programmy",
            "kartochnye-igry_",
            "logicheskie-igry",
            "muzykalnye-programmy",
            "obuchayushchie-programmy-",
            "operatsionnye-sistemy",
            "priklyuchencheskie-igry",
            "programmy-na-beysike",
            "programmy-po-elektronike",
            "redaktory",
            "reklamnye-programmy",
            "sborniki-na-disketakh",
            "sportivnye-igry",
            "sredstva-razrabotki",
            "telekommunikatsii",
            "utility",
            "yazyk-programmirovaniya-basic",
            "yazyk-programmirovaniya-pascal",
            "zagruzchiki"
        };
        for (String section : sections) {
            assertThat(VektorSections.placementOf(section).category())
                    .as(section)
                    .isNotNull();
        }
    }

    private static VektorPlacement imported(String category) {
        return new VektorPlacement(true, new CategoryId(UUID.fromString(category)));
    }
}
