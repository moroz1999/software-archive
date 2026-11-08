/** Texts for the business error codes deleting software can receive; others fall back by status. */
export const deleteSoftwareMessages: Readonly<Partial<Record<string, string>>> = {
    'software.not-found': 'This software has already been deleted.',
};
