import {type BusinessError, businessErrorMessage, type BusinessMessages} from '@/shared/api';

/** The texts of a refusal by the name of the field they go under. */
export type FieldRefusals = Readonly<Partial<Record<string, string>>>;

/** A refusal split into the texts under its fields and the text of the whole form, for what no field takes. */
export interface FormRefusal {
    readonly fields: FieldRefusals;
    readonly form: string | undefined;
}

/** Splits why a submit was refused between the form's fields and the form itself (docs/error-handling.md). */
export function splitRefusal(error: BusinessError | undefined, messages: BusinessMessages, fields: readonly string[]): FormRefusal {
    if (error === undefined) {
        return {fields: {}, form: undefined};
    }
    const texts: Record<string, string> = {};
    let fieldless = error.fieldErrors.length === 0;
    for (const fieldError of error.fieldErrors) {
        if (fields.includes(fieldError.field)) {
            texts[fieldError.field] = messages[fieldError.code] ?? businessErrorMessage(error, messages);
        } else {
            fieldless = true;
        }
    }
    return {fields: texts, form: fieldless ? businessErrorMessage(error, messages) : undefined};
}

/** The refusals of the fields not edited since the refusal arrived: an edit means it no longer applies. */
export function unedited(refusals: FieldRefusals, edited: ReadonlySet<string>): FieldRefusals {
    return Object.fromEntries(Object.entries(refusals).filter(([field]) => !edited.has(field)));
}
