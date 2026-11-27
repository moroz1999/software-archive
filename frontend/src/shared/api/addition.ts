/** Who added an entry of the archive and when: `by` is the user name, null once their account was deleted. */
export interface Addition {
    readonly by: string | null;
    /** The moment it was added, ISO-8601. */
    readonly at: string;
}
