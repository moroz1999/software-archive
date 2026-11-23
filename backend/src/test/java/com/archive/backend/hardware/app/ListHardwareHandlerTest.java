package com.archive.backend.hardware.app;

import static org.assertj.core.api.Assertions.assertThat;

import com.archive.backend.hardware.domain.PlatformCode;
import com.archive.backend.hardware.read.HardwareDetails;
import com.archive.backend.hardware.read.HardwareGroupDetails;
import com.archive.backend.hardware.read.HardwareGroupItem;
import com.archive.backend.hardware.read.HardwareItem;
import com.archive.backend.hardware.read.HardwareReadRepository;
import com.archive.backend.shared.domain.Language;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class ListHardwareHandlerTest {

    private static final HardwareItem VECTOR06C =
            new HardwareItem("vector06c", "Вектор-06Ц", "computers", "Компьютеры");

    private final HardwareReadRepository reads = new HardwareReadRepository() {
        @Override
        public List<HardwareItem> findByPlatform(String platform, String language) {
            return Map.of(
                            "vector:ru", List.of(VECTOR06C),
                            "vector:en", List.of(new HardwareItem("vector06c", "Vector-06C", "computers", "Computers")))
                    .getOrDefault(platform + ":" + language, List.of());
        }

        @Override
        public List<HardwareItem> findByCodes(Collection<String> codes, String language) {
            return List.of();
        }

        @Override
        public List<HardwareGroupItem> findGroups(String language) {
            return List.of();
        }

        @Override
        public Optional<HardwareGroupDetails> findGroup(String code) {
            return Optional.empty();
        }

        @Override
        public Optional<HardwareDetails> findHardware(String code) {
            return Optional.empty();
        }
    };

    @Test
    void returnsTheHardwareOfThePlatformInTheLanguageAskedFor() {
        assertThat(new ListHardwareHandler(reads)
                        .handle(new ListHardwareQuery(new PlatformCode("vector"), Language.RU)))
                .containsExactly(VECTOR06C);
    }
}
