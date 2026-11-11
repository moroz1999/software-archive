/** A label as an owner's form holds it: a new one has no id yet; `display` marks the display name. */
export interface LabelValue {
    readonly id: string | null;
    readonly name: string;
    readonly display: boolean;
}
