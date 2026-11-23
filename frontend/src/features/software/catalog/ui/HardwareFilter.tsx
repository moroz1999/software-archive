import {useState} from 'react';
import {useTranslation} from 'react-i18next';
import {type PlatformCode, toPlatformCode, usePlatformSelection} from '@/entities/platforms';
import {Button} from '@/shared/ui/button';
import {Checkbox} from '@/shared/ui/checkbox';
import {Dialog} from '@/shared/ui/dialog';
import {Grid} from '@/shared/ui/grid';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import type {FacetCount} from '../model/catalog';
import {useSoftwareCatalog} from '../model/software-catalog-context';

/** A checkbox of the window: what it filters by, its name and the count of software it leaves. */
interface Choice {
    readonly value: string;
    readonly label: string;
}

/** A group of checkboxes under its name, of the platforms or of the hardware of the filter. */
interface ChoiceGroup {
    readonly name: string;
    readonly of: keyof Draft;
    readonly choices: readonly Choice[];
}

/** What is checked in the window before it is applied. */
interface Draft {
    readonly platforms: readonly PlatformCode[];
    readonly hardware: readonly string[];
}

function toggled<T extends string>(values: readonly T[], value: T, checked: boolean): readonly T[] {
    return checked ? [...values, value] : values.filter((other) => other !== value);
}

function checkedIn(draft: Draft, of: keyof Draft, value: string): boolean {
    return of === 'platforms' ? draft.platforms.some((code) => code === value) : draft.hardware.includes(value);
}

function toggledIn(draft: Draft, of: keyof Draft, value: string, checked: boolean): Draft {
    return of === 'platforms'
        ? {...draft, platforms: toggled(draft.platforms, toPlatformCode(value), checked)}
        : {...draft, hardware: toggled(draft.hardware, value, checked)};
}

/** The platforms offered, the chosen ones kept even when they leave nothing. */
function withChosen(counts: readonly FacetCount<PlatformCode>[], chosen: readonly PlatformCode[]): readonly FacetCount<PlatformCode>[] {
    const missing = chosen.filter((code) => !counts.some((count) => count.value === code));
    return [...missing.map((value) => ({value, count: 0})), ...counts];
}

/**
 * The hardware the catalog is narrowed by, checked in a window and applied at once: the platforms while all of them
 * are shown in the switch, and the hardware the software found has, by group. Any item of a group will do; every group
 * checked is needed (docs/features/software-list.md).
 */
export function HardwareFilter() {
    const {t} = useTranslation();
    const {filter, catalog, platforms, changing, show} = useSoftwareCatalog();
    const {platform} = usePlatformSelection();
    const [draft, setDraft] = useState<Draft | null>(null);
    const facets = catalog.data?.facets;
    const platformNames = new Map<string, string>((platforms.data ?? []).map((item) => [item.code, item.name]));
    const hardwareNames = new Map((facets?.hardware ?? []).map((item) => [item.value, item.name]));
    // With one platform in the switch the platforms of the filter are not asked for.
    const shownPlatforms = platform === null ? filter.platforms : [];
    const chosenNames = [
        ...shownPlatforms.map((code) => platformNames.get(code) ?? code),
        ...filter.hardware.map((code) => hardwareNames.get(code) ?? code),
    ];

    const groups: ChoiceGroup[] = [];
    if (platform === null) {
        groups.push({
            name: t('filters.platforms'),
            of: 'platforms',
            choices: withChosen(facets?.platforms ?? [], filter.platforms).map(({value, count}) => ({
                value,
                label: t('filters.withCount', {label: platformNames.get(value) ?? value, count}),
            })),
        });
    }
    // The archive gives the hardware in the order of its groups.
    const hardwareGroups = new Map<string, {readonly name: string; readonly choices: Choice[]}>();
    for (const item of facets?.hardware ?? []) {
        const choice = {value: item.value, label: t('filters.withCount', {label: item.name, count: item.count})};
        const group = hardwareGroups.get(item.group);
        if (group === undefined) {
            hardwareGroups.set(item.group, {name: item.groupName, choices: [choice]});
        } else {
            group.choices.push(choice);
        }
    }
    for (const {name, choices} of hardwareGroups.values()) {
        groups.push({name, of: 'hardware', choices});
    }
    const close = () => {
        setDraft(null);
    };
    const apply = (next: Draft) => {
        setDraft(null);
        show({...filter, platforms: platform === null ? next.platforms : filter.platforms, hardware: next.hardware, page: 0});
    };

    return (
        <>
            <Button
                color="outlined"
                disabled={changing || facets === undefined}
                onClick={() => {
                    setDraft({platforms: shownPlatforms, hardware: filter.hardware});
                }}
            >
                {chosenNames.length === 0 ? t('filters.hardware') : t('filters.hardwareChosen', {names: chosenNames.join(', ')})}
            </Button>
            {draft !== null && (
                <Dialog
                    open
                    title={t('filters.hardware')}
                    onClose={close}
                    dismissible
                    width="wide"
                    actions={(
                        <>
                            <Button color="secondary" onClick={() => {
                                apply({platforms: [], hardware: []});
                            }}
                            >
                                {t('filters.reset')}
                            </Button>
                            <Button onClick={() => {
                                apply(draft);
                            }}
                            >
                                {t('filters.apply')}
                            </Button>
                        </>
                    )}
                >
                    <Grid desktopColumns={3} tabletColumns={2} mobileColumns={1} gap="lg">
                        {groups.filter((group) => group.choices.length > 0).map((group) => (
                            <div key={group.name} role="group" aria-label={group.name}>
                                <Stack gap="sm">
                                    <Text variant="label" tone="muted">{group.name}</Text>
                                    {group.choices.map((choice) => (
                                        <Checkbox
                                            key={choice.value}
                                            label={choice.label}
                                            checked={checkedIn(draft, group.of, choice.value)}
                                            onChange={(checked) => {
                                                setDraft(toggledIn(draft, group.of, choice.value, checked));
                                            }}
                                        />
                                    ))}
                                </Stack>
                            </div>
                        ))}
                    </Grid>
                </Dialog>
            )}
        </>
    );
}
