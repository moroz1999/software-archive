import {type LocationDraft, locationProblem, type LocationValue, toLocationDraft, toLocationValue} from '@/shared/lib/form-rules';
import {type TitleProblem, titleProblem} from '@/shared/lib/form-rules';
import type {MemberRoleValue} from '@/shared/ui/member-role-editor';
import type {GroupDetails} from './group';

/** A label in the form: a new one has no id yet; `display` stays unset, a group has no display name. */
export interface GroupFormLabel {
    readonly id: string | null;
    readonly name: string;
    readonly display: boolean;
}

/** A label of a group to be created or saved. */
export interface GroupDraftLabel {
    readonly id: string | null;
    readonly name: string;
}

/** A location in the form (docs/domain/geo.md). */
export type GroupFormLocation = LocationValue;

/** What the group form holds while it is being filled. */
export interface GroupFormValues {
    readonly name: string;
    readonly notes: string;
    /** The codes of the chosen types, in the order they were chosen. */
    readonly types: readonly string[];
    readonly labels: readonly GroupFormLabel[];
    readonly locations: readonly GroupFormLocation[];
    readonly members?: readonly MemberRoleValue[];
}

/** A group to be created or saved, as the form produced it. */
export interface GroupDraft {
    readonly name: string;
    readonly notes: string | null;
    readonly types: readonly string[];
    readonly labels: readonly GroupDraftLabel[];
    readonly locations: readonly LocationDraft[];
    readonly members?: readonly MemberRoleValue[];
}

export interface GroupFormErrors {
    readonly name?: TitleProblem;
    /** The problem of each label by its row; null for a label without one. */
    readonly labels?: readonly (TitleProblem | null)[];
    /** The problem of each location by its row; null for a location without one. */
    readonly locations?: readonly ('country-required' | null)[];
}

export type GroupFormResult =
    | {readonly ok: true; readonly draft: GroupDraft}
    | {readonly ok: false; readonly errors: GroupFormErrors};

/**
 * Checks the format rules of the form (docs/domain/authors.md): a group has a name, every label has a name and every
 * location a country; the types may be none. Empty optional fields become unknown.
 */
export function validateGroupForm(values: GroupFormValues): GroupFormResult {
    const name = titleProblem(values.name) ?? undefined;
    const labels = values.labels.map((label) => titleProblem(label.name));
    const locations = values.locations.map(locationProblem);
    const labelsWrong = labels.some((problem) => problem !== null);
    const locationsWrong = locations.some((problem) => problem !== null);
    if (name !== undefined || labelsWrong || locationsWrong) {
        return {
            ok: false,
            errors: {
                ...(name === undefined ? {} : {name}),
                ...(labelsWrong ? {labels} : {}),
                ...(locationsWrong ? {locations} : {}),
            },
        };
    }
    return {
        ok: true,
        draft: {
            name: values.name,
            notes: values.notes.trim() === '' ? null : values.notes,
            types: values.types,
            labels: values.labels.map((label) => ({id: label.id, name: label.name})),
            locations: values.locations.map(toLocationDraft),
            members: values.members ?? [],
        },
    };
}

/** The form values of an existing group, or of an empty form. */
export function toGroupFormValues(group?: GroupDetails): GroupFormValues {
    return {
        name: group?.name ?? '',
        notes: group?.notes ?? '',
        types: group?.types ?? [],
        labels: group?.labels.map((label) => ({id: label.id, name: label.name, display: false})) ?? [],
        locations: group?.locations.map(toLocationValue) ?? [],
        members: group?.members.flatMap((member) => member.id === null ? [] : [{
            id: member.id,
            type: member.type,
            title: member.title,
            roles: member.roles,
            startDate: member.startDate ?? '',
            endDate: member.endDate ?? '',
        }]) ?? [],
    };
}
