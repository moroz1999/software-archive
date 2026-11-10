/** Texts for the business error codes saving software can receive; others fall back by status. */
export const editSoftwareMessages: Readonly<Partial<Record<string, string>>> = {
    'hardware.unknown': 'Choose hardware of the platform of the software.',
    'software.platform-locked': 'Remove the hardware of the software and its releases first.',
};
