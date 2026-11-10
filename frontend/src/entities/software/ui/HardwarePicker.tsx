import {MultiSelectFilter} from '@/shared/ui/multi-select-filter';
import {groupHardware} from '../lib/hardware-groups';
import type {Hardware} from '../model/software';

export interface HardwarePickerProps {
    /** The hardware to choose from: of one platform. */
    readonly hardware: readonly Hardware[];
    /** The codes of the chosen hardware. */
    readonly value: readonly string[];
    readonly onChange: (value: readonly string[]) => void;
    readonly disabled?: boolean;
    readonly hint?: string;
    readonly error?: string | undefined;
}

/** Hardware of a platform chosen by group, for software and for its releases alike. */
export function HardwarePicker({hardware, value, onChange, disabled = false, hint, error}: HardwarePickerProps) {
    const groups = groupHardware(hardware).map((section) => ({
        label: section.label,
        options: section.items.map((item) => ({value: item.code, label: item.name})),
    }));
    return (
        <MultiSelectFilter
            label="Hardware"
            groups={groups}
            value={value}
            onChange={onChange}
            disabled={disabled}
            {...(hint === undefined ? {} : {hint})}
            error={error}
        />
    );
}
