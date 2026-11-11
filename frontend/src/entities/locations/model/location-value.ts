/** A location as a form holds it: the ids of the chosen country and city, each empty while not chosen. */
export interface LocationValue {
    readonly country: string;
    readonly city: string;
    readonly primary: boolean;
}
