import {NavChip, NavChips} from '@/shared/ui/nav-chips';
import {LABEL_LETTERS, type LabelLetter} from '../model/letter';

export interface LetterFilterProps {
    /** The chosen letter; null for all of them. */
    readonly current: LabelLetter | null;
    /** The address of the list narrowed to a letter, or not narrowed for null. */
    readonly hrefOf: (letter: LabelLetter | null) => string;
    readonly disabled: boolean;
}

/** "All" and every letter of the catalogs of names, the chosen one marked. */
export function LetterFilter({current, hrefOf, disabled}: LetterFilterProps) {
    const {t} = useTranslation();
    return (
        <NavChips label={t('filters.firstLetter')}>
            <NavChip href={hrefOf(null)} active={current === null} disabled={disabled}>{t('filters.all')}</NavChip>
            {LABEL_LETTERS.map((letter) => (
                <NavChip key={letter} href={hrefOf(letter)} active={current === letter} disabled={disabled}>
                    {letter}
                </NavChip>
            ))}
        </NavChips>
    );
}
import {useTranslation} from 'react-i18next';
