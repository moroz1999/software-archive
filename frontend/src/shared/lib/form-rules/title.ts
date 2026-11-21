/** The rules every title of the archive follows (docs/domain.md): required, not blank, at most 255 characters. */
export const TITLE_MAX_LENGTH = 255;

export type TitleProblem = 'required' | 'too-long';

export function titleProblem(raw: string): TitleProblem | null {
    if (raw.trim() === '') {
        return 'required';
    }
    return raw.length > TITLE_MAX_LENGTH ? 'too-long' : null;
}
