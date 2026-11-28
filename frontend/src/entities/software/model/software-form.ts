import {type TitleProblem, titleProblem} from '@/shared/lib/form-rules';
import {type components, lineageKindValues, lineageReliabilityValues, lineageReworkValues, preservationStatusValues} from '@/shared/api';
import type {MemberRoleValue} from '@/shared/ui/member-role-editor';
import {parseReleaseDateText, releaseDateField} from '../lib/release-date';
import {type PreservationStatus, type SoftwareDetails, type SoftwareLineage, type SoftwareProgram, type SoftwareReference} from './software';

/** A credit in the form: the person, group or label found, with the roles chosen for it. */
export type CreditValue = MemberRoleValue<SoftwareReference, components['schemas']['CreditRole']>;

/** The lineage in the form: the codes chosen, empty for unknown, and the text of the original. */
export interface LineageValues {
    readonly kind: string;
    readonly reliability: string;
    /** The codes of the rework, in the order they were checked. */
    readonly rework: readonly string[];
    readonly source: string;
}

/** The longest original the archive keeps (docs/domain/software.md#lineage). */
export const LINEAGE_SOURCE_MAX_LENGTH = 255;

const UNKNOWN_LINEAGE: LineageValues = {kind: '', reliability: '', rework: [], source: ''};

/** What the software form holds while it is being filled: raw text of every field, the chosen hardware and categories. */
export interface SoftwareFormValues {
    readonly title: string;
    readonly description: string;
    /** ISO `YYYY-MM-DD` from a date input, or empty. */
    readonly releaseDate: string;
    /** The code of the chosen platform, or empty. */
    readonly platform: string;
    /** The codes of the checked hardware. */
    readonly hardware: readonly string[];
    /** The ids of the checked categories. */
    readonly categories: readonly string[];
    readonly credits?: readonly CreditValue[];
    readonly compilationItems?: readonly SoftwareProgram[];
    /** The code of the chosen preservation status; unknown when absent. */
    readonly preservation?: string;
    readonly instructions?: string;
    readonly lineage?: LineageValues;
}

/** A platform the form offers. */
export interface PlatformChoice {
    readonly code: string;
    readonly name: string;
}

/** Software to be created or saved, as the form produced it. */
export interface SoftwareDraft {
    readonly title: string;
    readonly description: string | null;
    readonly releaseDate: string | null;
    readonly platform: string;
    readonly hardware: readonly string[];
    readonly categories: readonly string[];
    readonly credits: readonly CreditValue[];
    readonly compilationItemIds?: readonly string[];
    readonly preservation: PreservationStatus;
    readonly instructions: string | null;
    readonly lineage: SoftwareLineage;
}

export interface SoftwareFormErrors {
    readonly title?: TitleProblem;
    readonly releaseDate?: 'invalid';
    readonly platform?: 'required';
    readonly lineageSource?: 'too-long';
}

export type SoftwareFormResult =
    | {readonly ok: true; readonly draft: SoftwareDraft}
    | {readonly ok: false; readonly errors: SoftwareFormErrors};

/**
 * Checks the format rules of the form (docs/domain/software.md); empty optional fields become unknown. Software is
 * always for one platform, so the platform is required.
 */
export function validateSoftwareForm(values: SoftwareFormValues): SoftwareFormResult {
    const title = titleProblem(values.title);
    const releaseDate = values.releaseDate === '' ? null : parseReleaseDateText(values.releaseDate);
    const releaseDateInvalid = values.releaseDate !== '' && releaseDate === null;
    const platformMissing = values.platform === '';
    const lineage = values.lineage ?? UNKNOWN_LINEAGE;
    const sourceTooLong = lineage.source.length > LINEAGE_SOURCE_MAX_LENGTH;
    if (title !== null || releaseDateInvalid || platformMissing || sourceTooLong) {
        return {
            ok: false,
            errors: {
                ...(title === null ? {} : {title}),
                ...(releaseDateInvalid ? {releaseDate: 'invalid'} : {}),
                ...(platformMissing ? {platform: 'required'} : {}),
                ...(sourceTooLong ? {lineageSource: 'too-long'} : {}),
            },
        };
    }
    const description = values.description.trim();
    return {
        ok: true,
        draft: {
            title: values.title,
            description: description === '' ? null : values.description,
            releaseDate,
            platform: values.platform,
            hardware: values.hardware,
            categories: values.categories,
            credits: values.credits ?? [],
            preservation: preservationStatusValues.find((known) => known === values.preservation) ?? 'unknown',
            instructions: textOrNull(values.instructions),
            lineage: toLineage(lineage),
        },
    };
}

/** The lineage of the draft: the reliability and the rework tell of a kind, so none goes without one. */
function toLineage(values: LineageValues): SoftwareLineage {
    const kind = lineageKindValues.find((known) => known === values.kind) ?? null;
    return {
        kind,
        reliability: kind === null ? null : lineageReliabilityValues.find((known) => known === values.reliability) ?? null,
        rework: kind === null ? [] : values.rework.flatMap((code) => lineageReworkValues.filter((known) => known === code)),
        source: textOrNull(values.source),
    };
}

/** The form values of existing software, or of an empty form. */
export function toSoftwareFormValues(software?: SoftwareDetails): SoftwareFormValues {
    return {
        title: software?.title ?? '',
        description: software?.description ?? '',
        releaseDate: releaseDateField(software?.releaseDate),
        platform: software?.platform.code ?? '',
        hardware: software?.hardware.map((item) => item.code) ?? [],
        categories: software?.categories.map((category) => category.id) ?? [],
        credits: software?.credits ?? [],
        ...(software?.compilationItems === undefined ? {} : {compilationItems: software.compilationItems}),
        preservation: software?.preservation ?? 'unknown',
        instructions: software?.instructions ?? '',
        lineage: software === undefined ? UNKNOWN_LINEAGE : {
            kind: software.lineage.kind ?? '',
            reliability: software.lineage.reliability ?? '',
            rework: software.lineage.rework,
            source: software.lineage.source ?? '',
        },
    };
}

/** A text as given, or none when it holds nothing but spaces. */
function textOrNull(value: string | undefined): string | null {
    return value === undefined || value.trim() === '' ? null : value;
}
