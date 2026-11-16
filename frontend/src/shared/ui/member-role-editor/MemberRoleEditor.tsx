import {type KeyboardEvent, useId, useRef, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {AnchoredOverlay} from '../anchored-overlay';
import {Button} from '../button';
import {Chip} from '../chip';
import {FormField} from '../form';
import {CloseIcon} from '../icon';
import {Input} from '../input';
import {MultiEntityAutocomplete, type EntityAutocompleteOption} from '../multi-entity-autocomplete';
import {Text} from '../typography';
import styles from './MemberRoleEditor.module.css';

export interface MemberRoleValue extends EntityAutocompleteOption {
    readonly roles: readonly string[];
    readonly startDate: string;
    readonly endDate: string;
}

export interface MemberRoleOption {
    readonly code: string;
    readonly title: string;
}

export interface MemberRoleEditorProps {
    readonly label: string;
    readonly value: readonly MemberRoleValue[];
    readonly roles: readonly MemberRoleOption[];
    readonly search: (query: string, signal: AbortSignal) => Promise<readonly EntityAutocompleteOption[]>;
    readonly onChange: (value: readonly MemberRoleValue[]) => void;
    readonly showDates?: boolean;
    readonly disabled?: boolean;
    readonly error?: string | undefined;
}

/** People or their aliases as a grid of cards, each with the roles chosen for it; new ones are found under the grid. */
export function MemberRoleEditor({label, value, roles, search, onChange, showDates = false, disabled = false, error}: MemberRoleEditorProps) {
    function add(options: readonly EntityAutocompleteOption[]) {
        const option = options[0];
        // A member already on a card is not added twice.
        if (option === undefined || value.some((member) => member.type === option.type && member.id === option.id)) {
            return;
        }
        onChange([...value, {...option, roles: [], startDate: '', endDate: ''}]);
    }

    function replace(index: number, member: MemberRoleValue) {
        onChange(value.map((current, currentIndex) => currentIndex === index ? member : current));
    }

    return (
        <div className={styles.root}>
            {value.length > 0 && (
                <div className={styles.grid}>
                    {value.map((member, index) => (
                        <MemberCard
                            key={`${member.type}:${member.id}`}
                            member={member}
                            roles={roles}
                            showDates={showDates}
                            disabled={disabled}
                            onChange={(next) => { replace(index, next); }}
                            onRemove={() => { onChange(value.filter((_, currentIndex) => currentIndex !== index)); }}
                        />
                    ))}
                </div>
            )}
            <FormField label={label} error={error}>
                {(control) => (
                    <MultiEntityAutocomplete
                        {...control}
                        value={[]}
                        onChange={add}
                        load={search}
                        queryKey={`member-role-${label}`}
                        disabled={disabled}
                    />
                )}
            </FormField>
        </div>
    );
}

function MemberCard({member, roles, showDates, disabled, onChange, onRemove}: {
    readonly member: MemberRoleValue;
    readonly roles: readonly MemberRoleOption[];
    readonly showDates: boolean;
    readonly disabled: boolean;
    readonly onChange: (value: MemberRoleValue) => void;
    readonly onRemove: () => void;
}) {
    const {t} = useTranslation();
    const id = useId();

    return (
        <div className={styles.card}>
            <div className={styles.head}>
                <Text as="span" variant="bodySm" tone="strong" truncate className={styles.name}>{member.title}</Text>
                <Button size="xs" color="transparent" square ariaLabel={t('selection.remove', {name: member.title})} onClick={onRemove} disabled={disabled}>
                    <CloseIcon/>
                </Button>
            </div>
            {member.roles.length > 0 && (
                <div className={styles.roles}>
                    {member.roles.map((code) => (
                        <Chip
                            key={code}
                            size="sm"
                            variant="filled"
                            disabled={disabled}
                            onRemove={() => { onChange({...member, roles: member.roles.filter((role) => role !== code)}); }}
                            removeLabel={t('selection.remove', {name: roleTitle(roles, code)})}
                        >
                            {roleTitle(roles, code)}
                        </Chip>
                    ))}
                </div>
            )}
            <RolePicker
                id={`${id}-role`}
                label={t('roles.for', {name: member.title})}
                roles={roles.filter((role) => !member.roles.includes(role.code))}
                disabled={disabled}
                onPick={(code) => { onChange({...member, roles: [...member.roles, code]}); }}
            />
            {showDates && (
                <div className={styles.dates}>
                    <Input
                        id={`${id}-start`}
                        value={member.startDate}
                        onChange={(startDate) => { onChange({...member, startDate}); }}
                        ariaLabel={t('roles.startDateFor', {name: member.title})}
                        placeholder={t('roles.from')}
                        disabled={disabled}
                    />
                    <Input
                        id={`${id}-end`}
                        value={member.endDate}
                        onChange={(endDate) => { onChange({...member, endDate}); }}
                        ariaLabel={t('roles.endDateFor', {name: member.title})}
                        placeholder={t('roles.to')}
                        disabled={disabled}
                    />
                </div>
            )}
        </div>
    );
}

/** A search field over the roles left to choose; it stays open after a pick, so roles are added one after another. */
function RolePicker({id, label, roles, disabled, onPick}: {
    readonly id: string;
    readonly label: string;
    readonly roles: readonly MemberRoleOption[];
    readonly disabled: boolean;
    readonly onPick: (code: string) => void;
}) {
    const {t} = useTranslation();
    const [query, setQuery] = useState('');
    const [open, setOpen] = useState(false);
    const [active, setActive] = useState(-1);
    const anchor = useRef<HTMLDivElement>(null);
    const listId = `${id}-list`;
    const needle = query.trim().toLocaleLowerCase();
    const options = roles.filter((role) => role.title.toLocaleLowerCase().includes(needle));

    function pick(role: MemberRoleOption) {
        onPick(role.code);
        setQuery('');
        setActive(-1);
    }

    function onKeyDown(event: KeyboardEvent<HTMLInputElement>) {
        if (event.key === 'ArrowDown') {
            event.preventDefault();
            setOpen(true);
            setActive((current) => Math.min(current + 1, options.length - 1));
        } else if (event.key === 'ArrowUp') {
            event.preventDefault();
            setActive((current) => Math.max(current - 1, 0));
        } else if (event.key === 'Enter') {
            // Enter in the field picks a role and never submits the form around it.
            event.preventDefault();
            const role = options[active];
            if (open && role !== undefined) {
                pick(role);
            }
        } else if (event.key === 'Escape') {
            setOpen(false);
        }
    }

    return (
        <>
            <div ref={anchor}>
                <Input
                    id={id}
                    type="search"
                    value={query}
                    onChange={(next) => {
                        setQuery(next);
                        setOpen(true);
                        setActive(-1);
                    }}
                    placeholder={t('roles.add')}
                    role="combobox"
                    ariaLabel={label}
                    ariaAutocomplete="list"
                    ariaControls={listId}
                    ariaExpanded={open}
                    ariaActiveDescendant={open && options[active] !== undefined ? `${listId}-${String(active)}` : undefined}
                    disabled={disabled || roles.length === 0}
                    onKeyDown={onKeyDown}
                    onFocus={() => { setOpen(true); }}
                    onBlur={() => {
                        setOpen(false);
                        setQuery('');
                        setActive(-1);
                    }}
                />
            </div>
            <AnchoredOverlay anchor={anchor} open={open} className={styles.overlay}>
                <div id={listId} role="listbox" aria-label={label} className={styles.list}>
                    {options.length === 0 && (
                        <div className={styles.empty}><Text as="span" variant="bodySm" tone="muted">{t('empty.nothingFound')}</Text></div>
                    )}
                    {options.map((role, index) => (
                        <button
                            id={`${listId}-${String(index)}`}
                            key={role.code}
                            type="button"
                            role="option"
                            aria-selected={index === active}
                            className={styles.option}
                            onMouseDown={(event) => {
                                // The field keeps the focus, so the list stays open for the next role.
                                event.preventDefault();
                            }}
                            onMouseEnter={() => { setActive(index); }}
                            onClick={() => { pick(role); }}
                        >
                            <Text as="span" variant="bodySm">{role.title}</Text>
                        </button>
                    ))}
                </div>
            </AnchoredOverlay>
        </>
    );
}

function roleTitle(roles: readonly MemberRoleOption[], code: string): string {
    return roles.find((role) => role.code === code)?.title ?? code;
}
