import {useQuery} from '@tanstack/react-query';
import {type KeyboardEvent, useEffect, useId, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {AnchoredOverlay} from '../anchored-overlay';
import {Chip} from '../chip';
import {Input} from '../input';
import {Spinner} from '../spinner';
import {Text} from '../typography';
import styles from './MultiEntityAutocomplete.module.css';

export interface EntityAutocompleteOption {
    readonly id: string;
    readonly type: string;
    readonly title: string;
}

export interface MultiEntityAutocompleteProps<T extends EntityAutocompleteOption> {
    readonly id: string;
    readonly value: readonly T[];
    readonly onChange: (value: readonly T[]) => void;
    readonly load: (query: string, signal: AbortSignal) => Promise<readonly T[]>;
    readonly queryKey: string;
    readonly invalid?: boolean;
    readonly describedBy?: string | undefined;
    readonly disabled?: boolean;
}

const DEBOUNCE_MS = 300;

/** A chip multi-select whose options are found remotely as the user types. */
export function MultiEntityAutocomplete<T extends EntityAutocompleteOption>({id, value, onChange, load, queryKey, invalid = false, describedBy, disabled = false}: MultiEntityAutocompleteProps<T>) {
    const {t} = useTranslation();
    const [query, setQuery] = useState('');
    const [debounced, setDebounced] = useState('');
    const [open, setOpen] = useState(false);
    const [active, setActive] = useState(-1);
    const anchor = useRef<HTMLDivElement>(null);
    const listId = useId();

    useEffect(() => {
        const timer = window.setTimeout(() => {
            setDebounced(query.trim());
        }, DEBOUNCE_MS);
        return () => {
            window.clearTimeout(timer);
        };
    }, [query]);

    const result = useQuery({
        queryKey: ['entity-autocomplete', queryKey, debounced],
        queryFn: ({signal}) => load(debounced, signal),
        enabled: debounced !== '',
    });
    const chosen = new Set(value.map(keyOf));
    const options = (result.data ?? []).filter((option) => !chosen.has(keyOf(option)));
    const shown = open && query.trim() !== '';
    const loading = query.trim() !== debounced || result.isFetching;

    function choose(option: T) {
        onChange([...value, option]);
        setQuery('');
        setDebounced('');
        setOpen(false);
        setActive(-1);
    }

    function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
        if (event.key === 'ArrowDown') {
            event.preventDefault();
            setOpen(true);
            setActive((current) => Math.min(current + 1, Math.max(options.length - 1, 0)));
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            setActive((current) => Math.max(current - 1, 0));
        } else if (event.key === 'Enter' && shown && options[active] !== undefined) {
            event.preventDefault();
            choose(options[active]);
        } else if (event.key === 'Escape') {
            setOpen(false);
        }
    }

    return (
        <div className={styles.root}>
            {value.length > 0 && (
                <div className={styles.chips}>
                    {value.map((option) => (
                        <Chip
                            key={keyOf(option)}
                            variant="filled"
                            onRemove={() => {
                                onChange(value.filter((item) => keyOf(item) !== keyOf(option)));
                            }}
                            removeLabel={t('selection.remove', {name: option.title})}
                            disabled={disabled}
                        >
                            {option.title}
                        </Chip>
                    ))}
                </div>
            )}
            <div ref={anchor} className={styles.anchor}>
                <Input
                    id={id}
                    type="search"
                    value={query}
                    onChange={(next) => {
                        setQuery(next);
                        setOpen(next.trim() !== '');
                        setActive(-1);
                    }}
                    role="combobox"
                    ariaAutocomplete="list"
                    ariaControls={listId}
                    ariaExpanded={shown}
                    ariaActiveDescendant={shown && options[active] !== undefined ? `${listId}-${String(active)}` : undefined}
                    invalid={invalid}
                    describedBy={describedBy}
                    disabled={disabled}
                    onKeyDown={onKeyDown}
                    onFocus={() => {
                        setOpen(query.trim() !== '');
                    }}
                    onBlur={() => {
                        setOpen(false);
                    }}
                />
            </div>
            <AnchoredOverlay anchor={anchor} open={shown} className={styles.overlay}>
                <div id={listId} role="listbox" className={styles.list}>
                    {loading && <div className={styles.status}><Spinner size="sm" label={t('loading.searching')}/></div>}
                    {!loading && !result.isError && options.length === 0 && <div className={styles.status}><Text as="span" variant="bodySm" tone="muted">{t('empty.nothingFound')}</Text></div>}
                    {!loading && !result.isError && options.map((option, index) => (
                        <button
                            id={`${listId}-${String(index)}`}
                            key={keyOf(option)}
                            type="button"
                            role="option"
                            aria-selected={index === active}
                            className={styles.option}
                            onMouseDown={(event) => {
                                event.preventDefault();
                            }}
                            onClick={() => {
                                choose(option);
                            }}
                        >
                            <Text as="span" variant="bodySm">{option.title}</Text>
                        </button>
                    ))}
                </div>
            </AnchoredOverlay>
        </div>
    );
}

function keyOf(option: EntityAutocompleteOption): string {
    return `${option.type}:${option.id}`;
}
