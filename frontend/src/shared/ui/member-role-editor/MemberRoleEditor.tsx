import {useId, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '../button';
import {Chip} from '../chip';
import {Field} from '../field';
import {Inline} from '../inline';
import {Input} from '../input';
import {MultiEntityAutocomplete, type EntityAutocompleteOption} from '../multi-entity-autocomplete';
import {Stack} from '../stack';
import {Text} from '../typography';

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

/** People or their aliases, with several roles on each selected person. */
export function MemberRoleEditor({label, value, roles, search, onChange, showDates = false, disabled = false, error}: MemberRoleEditorProps) {
    function add(options: readonly EntityAutocompleteOption[]) {
        const option = options[0];
        if (option === undefined) {
            return;
        }
        onChange([...value, {...option, roles: [], startDate: '', endDate: ''}]);
    }

    function replace(index: number, member: MemberRoleValue) {
        onChange(value.map((current, currentIndex) => currentIndex === index ? member : current));
    }

    return (
        <Stack gap="md">
            <Field label={label} error={error}>
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
            </Field>
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
        </Stack>
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
    const roleInputId = useId();
    const [adding, setAdding] = useState(false);
    const [query, setQuery] = useState('');
    const selected = new Set(member.roles);
    const available = roles.filter((role) => !selected.has(role.code) && role.title.toLocaleLowerCase().includes(query.trim().toLocaleLowerCase()));

    function changeRoles(next: readonly string[]) {
        onChange({...member, roles: next});
    }

    return (
        <Stack gap="sm">
            <Inline gap="sm" justify="between" align="center" wrap>
                <Text variant="label">{member.title}</Text>
                <Button size="xs" color="transparent" onClick={onRemove} disabled={disabled}>{t('actions.remove')}</Button>
            </Inline>
            {member.roles.length > 0 && (
                <Inline gap="xs" wrap>
                    {member.roles.map((code) => (
                        <Chip
                            key={code}
                            size="sm"
                            variant="filled"
                            disabled={disabled}
                            onRemove={() => { changeRoles(member.roles.filter((role) => role !== code)); }}
                            removeLabel={`Remove ${roleTitle(roles, code)}`}
                        >
                            {roleTitle(roles, code)}
                        </Chip>
                    ))}
                </Inline>
            )}
            <div>
                <Button
                    size="xs"
                    color="outlined"
                    ariaLabel={t('roles.addTo', {name: member.title})}
                    disabled={disabled || available.length === 0}
                    onClick={() => { setAdding((open) => !open); }}
                >
                    {t('roles.add')}
                </Button>
                {adding && (
                    <Stack gap="xs">
                        <Input
                            id={roleInputId}
                            type="search"
                            value={query}
                            onChange={setQuery}
                            role="combobox"
                            ariaLabel={t('roles.for', {name: member.title})}
                            ariaExpanded
                        />
                        <div role="listbox">
                            {available.map((role) => (
                                <button
                                    key={role.code}
                                    type="button"
                                    role="option"
                                    aria-selected="false"
                                    onClick={() => {
                                        changeRoles([...member.roles, role.code]);
                                        setAdding(false);
                                        setQuery('');
                                    }}
                                >
                                    {role.title}
                                </button>
                            ))}
                        </div>
                    </Stack>
                )}
            </div>
            {showDates && (
                <Inline gap="sm" wrap>
                    <Input
                        id={`${roleInputId}-start`}
                        value={member.startDate}
                        onChange={(startDate) => { onChange({...member, startDate}); }}
                        ariaLabel={t('roles.startDateFor', {name: member.title})}
                        placeholder="YYYY-MM-DD"
                        disabled={disabled}
                    />
                    <Input
                        id={`${roleInputId}-end`}
                        value={member.endDate}
                        onChange={(endDate) => { onChange({...member, endDate}); }}
                        ariaLabel={t('roles.endDateFor', {name: member.title})}
                        placeholder="YYYY-MM-DD"
                        disabled={disabled}
                    />
                </Inline>
            )}
        </Stack>
    );
}

function roleTitle(roles: readonly MemberRoleOption[], code: string): string {
    return roles.find((role) => role.code === code)?.title ?? code;
}
