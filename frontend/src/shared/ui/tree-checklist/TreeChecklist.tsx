import {useId, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {classNames} from '@/shared/lib/class-names';
import {Checkbox} from '../checkbox';
import {Chip} from '../chip';
import {Input} from '../input';
import {Text} from '../typography';
import styles from './TreeChecklist.module.css';

export interface TreeChecklistItem {
    readonly id: string;
    readonly name: string;
    readonly children: readonly TreeChecklistItem[];
}

export interface TreeChecklistProps {
    /** The name of the whole choice. */
    readonly label: string;
    readonly items: readonly TreeChecklistItem[];
    /** The ids of the chosen items. */
    readonly value: readonly string[];
    /** The chosen ids now, in the order they were chosen. */
    readonly onChange: (value: readonly string[]) => void;
    readonly disabled?: boolean;
    /** Why the choice was refused, shown under it. */
    readonly error?: string | undefined;
    /** External contour only; see docs/styles.md. */
    readonly className?: string;
}

/** Every item with its name, wherever it is in the tree. */
function namesOf(items: readonly TreeChecklistItem[], names = new Map<string, string>()): Map<string, string> {
    items.forEach((item) => {
        names.set(item.id, item.name);
        namesOf(item.children, names);
    });
    return names;
}

/** The items whose names hold the needle, each under the parents that lead to it. */
function filterTree(items: readonly TreeChecklistItem[], needle: string): readonly TreeChecklistItem[] {
    return items.flatMap((item) => {
        const children = filterTree(item.children, needle);
        return children.length > 0 || item.name.toLowerCase().includes(needle) ? [{...item, children}] : [];
    });
}

/**
 * Many items chosen from a tree: the chosen ones as removable chips, a filter by name and a scrolling list of
 * checkboxes, each under its parent. Choosing a parent does not choose its children.
 */
export function TreeChecklist({label, items, value, onChange, disabled = false, error, className}: TreeChecklistProps) {
    const {t} = useTranslation();
    const [query, setQuery] = useState('');
    const searchId = useId();
    const errorId = useId();
    const names = namesOf(items);
    const needle = query.trim().toLowerCase();
    const shown = needle === '' ? items : filterTree(items, needle);

    function toggle(id: string, checked: boolean) {
        const rest = value.filter((item) => item !== id);
        onChange(checked ? [...rest, id] : rest);
    }

    return (
        <fieldset className={classNames(styles.checklist, className)} aria-describedby={error === undefined ? undefined : errorId}>
            <legend className={styles.legend}>{label}</legend>
            {value.length > 0 && (
                <div className={styles.chips}>
                    {value.map((id) => {
                        const name = names.get(id) ?? id;
                        return (
                            <Chip
                                key={id}
                                variant="filled"
                                onRemove={() => {
                                    toggle(id, false);
                                }}
                                removeLabel={t('selection.remove', {name})}
                                disabled={disabled}
                            >
                                {name}
                            </Chip>
                        );
                    })}
                </div>
            )}
            <Input id={searchId} type="search" value={query} onChange={setQuery} ariaLabel={t('filters.filterLabel', {label})} placeholder={t('filters.filter')} disabled={disabled}/>
            <div className={styles.list}>
                {shown.length === 0
                    ? <Text as="span" variant="bodySm" tone="muted">{t('empty.nothingFound')}</Text>
                    : <Level items={shown} value={value} onToggle={toggle} disabled={disabled} top/>}
            </div>
            <Text as="span" variant="caption" tone="muted">{t('selection.selected', {count: value.length})}</Text>
            {error !== undefined && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
        </fieldset>
    );
}

interface LevelProps {
    readonly items: readonly TreeChecklistItem[];
    readonly value: readonly string[];
    readonly onToggle: (id: string, checked: boolean) => void;
    readonly disabled: boolean;
    /** The top of the tree: its items stand apart from each other. */
    readonly top?: boolean;
}

function Level({items, value, onToggle, disabled, top = false}: LevelProps) {
    return (
        <ul className={top ? styles.top : styles.level}>
            {items.map((item) => {
                const box = (
                    <Checkbox
                        label={item.name}
                        checked={value.includes(item.id)}
                        onChange={(checked) => {
                            onToggle(item.id, checked);
                        }}
                        disabled={disabled}
                    />
                );
                return (
                    <li key={item.id} className={styles.item}>
                        {item.children.length === 0
                            ? box
                            : (
                                <div role="group" aria-label={item.name}>
                                    {box}
                                    <Level items={item.children} value={value} onToggle={onToggle} disabled={disabled}/>
                                </div>
                            )}
                    </li>
                );
            })}
        </ul>
    );
}
