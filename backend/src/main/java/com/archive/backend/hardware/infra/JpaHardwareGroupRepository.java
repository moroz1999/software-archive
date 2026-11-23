package com.archive.backend.hardware.infra;

import com.archive.backend.hardware.domain.HardwareGroup;
import com.archive.backend.hardware.domain.HardwareGroupCode;
import com.archive.backend.hardware.domain.HardwareGroupCodeTakenException;
import com.archive.backend.hardware.domain.HardwareGroupRepository;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JpaHardwareGroupRepository implements HardwareGroupRepository {

    private final SpringDataHardwareGroupRepository rows;

    @Override
    public Optional<HardwareGroup> findByCode(HardwareGroupCode code) {
        return rows.findById(code.value())
                .map(entity -> new HardwareGroup(code, HardwareNamesRows.toNames(entity.getNames())));
    }

    @Override
    public void add(HardwareGroup group) {
        if (rows.existsById(group.getCode().value())) {
            throw new HardwareGroupCodeTakenException();
        }
        HardwareGroupEntity entity = new HardwareGroupEntity();
        entity.setCode(group.getCode().value());
        entity.setPosition(rows.nextPosition());
        write(group, entity);
    }

    @Override
    public void save(HardwareGroup group) {
        write(group, rows.findById(group.getCode().value()).orElseThrow());
    }

    private void write(HardwareGroup group, HardwareGroupEntity entity) {
        entity.getNames().clear();
        entity.getNames().putAll(HardwareNamesRows.toRows(group.getNames()));
        rows.saveAndFlush(entity);
    }

    @Override
    public void delete(HardwareGroup group) {
        rows.deleteById(group.getCode().value());
        rows.flush();
    }
}
