package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.domain.Hardware;
import com.archive.backend.hardware.domain.HardwareCode;
import com.archive.backend.hardware.domain.HardwareCodeTakenException;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.domain.HardwarePlacement;
import com.archive.backend.hardware.domain.HardwarePlatforms;
import com.archive.backend.hardware.domain.HardwareRepository;
import com.archive.backend.hardware.domain.PlatformCode;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaHardwareRepository implements HardwareRepository {

    private final SpringDataHardwareRepository rows;

    @Override
    public Optional<Hardware> findByCode(HardwareCode code) {
        return rows.findById(code.value())
                .map(entity -> new Hardware(
                        code,
                        HardwareNamesRows.toNames(entity.getNames()),
                        HardwarePlacement.restore(
                                new HardwareGroupCode(entity.getGroup()),
                                new HardwarePlatforms(entity.getPlatforms().stream()
                                        .map(PlatformCode::new)
                                        .collect(Collectors.toSet())))));
    }

    @Override
    public void add(Hardware hardware) {
        if (rows.existsById(hardware.getCode().value())) {
            throw new HardwareCodeTakenException();
        }
        HardwareEntity entity = new HardwareEntity();
        entity.setCode(hardware.getCode().value());
        write(hardware, entity);
    }

    @Override
    public void save(Hardware hardware) {
        write(hardware, rows.findById(hardware.getCode().value()).orElseThrow());
    }

    private void write(Hardware hardware, HardwareEntity entity) {
        entity.setGroup(hardware.getPlacement().group().value());
        entity.getNames().clear();
        entity.getNames().putAll(HardwareNamesRows.toRows(hardware.getNames()));
        entity.getPlatforms().clear();
        hardware.getPlacement()
                .platforms()
                .forEach(platform -> entity.getPlatforms().add(platform.value()));
        rows.saveAndFlush(entity);
    }

    @Override
    public void delete(Hardware hardware) {
        rows.deleteById(hardware.getCode().value());
        rows.flush();
    }
}
