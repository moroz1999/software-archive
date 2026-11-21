import {type TitleProblem, titleProblem} from '@/shared/lib/form-rules';
import type {MemberRoleValue} from '@/shared/ui/member-role-editor';
import {parseReleaseDateText, releaseDateField} from '../lib/release-date';
import {
    type PreservationStatus,
    type ReleaseDate,
    type SoftwareDetails,
    type SoftwarePlatformCode,
    type SoftwareTitle,
    toPreservationStatus,
    toSoftwarePlatformCode,
    toSoftwareTitle,
} from './software';

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
    readonly credits?: readonly MemberRoleValue[];
    /** The code of the chosen preservation status; unknown when absent. */
    readonly preservation?: string;
}

/** A platform the form offers. */
export interface PlatformChoice {
    readonly code: string;
    readonly name: string;
}

/** Software to be created or saved, as the form produced it. */
export interface SoftwareDraft {
    readonly title: SoftwareTitle;
    readonly description: string | null;
    readonly releaseDate: ReleaseDate | null;
    readonly platform: SoftwarePlatformCode;
    readonly hardware: readonly string[];
    readonly categories: readonly string[];
    readonly credits: readonly MemberRoleValue[];
    readonly preservation: PreservationStatus;
}

export interface SoftwareFormErrors {
    readonly title?: TitleProblem;
    readonly releaseDate?: 'invalid';
    readonly platform?: 'required';
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
    if (title !== null || releaseDateInvalid || platformMissing) {
        return {
            ok: false,
            errors: {
                ...(title === null ? {} : {title}),
                ...(releaseDateInvalid ? {releaseDate: 'invalid'} : {}),
                ...(platformMissing ? {platform: 'required'} : {}),
            },
        };
    }
    const description = values.description.trim();
    return {
        ok: true,
        draft: {
            title: toSoftwareTitle(values.title),
            description: description === '' ? null : values.description,
            releaseDate,
            platform: toSoftwarePlatformCode(values.platform),
            hardware: values.hardware,
            categories: values.categories,
            credits: values.credits ?? [],
            preservation: toPreservationStatus(values.preservation ?? 'unknown'),
        },
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
        preservation: software?.preservation ?? 'unknown',
    };
}
