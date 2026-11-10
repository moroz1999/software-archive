import {useId, useState} from 'react';
import {classNames} from '@/shared/lib/class-names';
import {Checkbox} from '../checkbox';
import {Chip} from '../chip';
import {Input} from '../input';
import {Text} from '../typography';
import styles from './MultiSelectFilter.module.css';

export interface MultiSelectOption {
    readonly value: string;
    readonly label: string;
}

/** Options under a common name. */
export interface MultiSelectGroup {
    readonly label: string;
    readonly options: readonly MultiSelectOption[];
}

export interface MultiSelectFilterProps {
    /** The name of the whole choice. */
    readonly label: string;
    readonly groups: readonly MultiSelectGroup[];
    readonly value: readonly string[];
    /** The chosen values now, in the order they were chosen. */
    readonly onChange: (value: readonly string[]) => void;
    readonly disabled?: boolean;
    /** What to know about the choice, shown under it. */
    readonly hint?: string;
    /** Why the choice was refused, shown under it. */
    readonly error?: string | undefined;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/**
 * Many values chosen from a long list: the chosen ones as removable chips, a filter by name and a scrolling list of
 * checkboxes under the names of their groups.
 */
export function MultiSelectFilter({label, groups, value, onChange, disabled = false, hint, error, className}: MultiSelectFilterProps) {
    const [query, setQuery] = useState('');
    const searchId = useId();
    const hintId = useId();
    const errorId = useId();
    const labels = new Map(groups.flatMap((group) => group.options.map((option) => [option.value, option.label] as const)));
    const needle = query.trim().toLowerCase();
    const shown = groups
        .map((group) => ({...group, options: group.options.filter((option) => option.label.toLowerCase().includes(needle))}))
        .filter((group) => group.options.length > 0);
    const describedBy = [hint === undefined ? null : hintId, error === undefined ? null : errorId].filter((id) => id !== null).join(' ');

    function toggle(option: string, checked: boolean) {
        const rest = value.filter((item) => item !== option);
        onChange(checked ? [...rest, option] : rest);
    }

    return (
        <fieldset className={classNames(styles.filter, className)} aria-describedby={describedBy === '' ? undefined : describedBy}>
            <legend className={styles.legend}>{label}</legend>
            {value.length > 0 && (
                <div className={styles.chips}>
                    {value.map((item) => {
                        const name = labels.get(item) ?? item;
                        return (
                            <Chip
                                key={item}
                                variant="filled"
                                onRemove={() => {
                                    toggle(item, false);
                                }}
                                removeLabel={`Remove ${name}`}
                                disabled={disabled}
                            >
                                {name}
                            </Chip>
                        );
                    })}
                </div>
            )}
            <Input id={searchId} type="search" value={query} onChange={setQuery} ariaLabel={`Filter ${label}`} placeholder="Filter" disabled={disabled}/>
            <div className={styles.list}>
                {shown.length === 0 && <Text as="span" variant="bodySm" tone="muted">Nothing found.</Text>}
                {shown.map((group) => <Group key={group.label} group={group} value={value} onToggle={toggle} disabled={disabled}/>)}
            </div>
            <Text as="span" variant="caption" tone="muted">{value.length} selected</Text>
            {hint !== undefined && <Text as="span" id={hintId} variant="caption" tone="muted">{hint}</Text>}
            {error !== undefined && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
        </fieldset>
    );
}

interface GroupProps {
    readonly group: MultiSelectGroup;
    readonly value: readonly string[];
    readonly onToggle: (option: string, checked: boolean) => void;
    readonly disabled: boolean;
}

function Group({group, value, onToggle, disabled}: GroupProps) {
    const headerId = useId();
    return (
        <div role="group" aria-labelledby={headerId} className={styles.group}>
            <span id={headerId} className={styles.groupName}>{group.label}</span>
            <div className={styles.options}>
                {group.options.map((option) => (
                    <Checkbox
                        key={option.value}
                        label={option.label}
                        checked={value.includes(option.value)}
                        onChange={(checked) => {
                            onToggle(option.value, checked);
                        }}
                        disabled={disabled}
                    />
                ))}
            </div>
        </div>
    );
}
