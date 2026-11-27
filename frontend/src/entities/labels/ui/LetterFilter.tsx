import {NavChip, NavChipRow, NavChips} from '@/shared/ui/nav-chips';
import {CYRILLIC_LABEL_LETTERS, LATIN_LABEL_LETTERS, REST_LABEL_LETTER} from '../model/letter';

export interface LetterFilterProps {
    /** The chosen letter; null for all of them. */
    readonly current: string | null;
    /** The address of the list narrowed to a letter, or not narrowed for null. */
    readonly hrefOf: (letter: string | null) => string;
    readonly disabled: boolean;
}

/** "All" and every letter of the catalogs of names, the chosen one marked. */
export function LetterFilter({current, hrefOf, disabled}: LetterFilterProps) {
    const {t} = useTranslation();
    return (
        <NavChips label={t('filters.firstLetter')}>
            <NavChipRow>
                <NavChip href={hrefOf(null)} active={current === null} disabled={disabled}>{t('filters.all')}</NavChip>
                {LATIN_LABEL_LETTERS.map((letter) => (
                    <NavChip key={letter} href={hrefOf(letter)} active={current === letter} disabled={disabled}>
                        {letter}
                    </NavChip>
                ))}
                <NavChip href={hrefOf(REST_LABEL_LETTER)} active={current === REST_LABEL_LETTER} disabled={disabled}>
                    {REST_LABEL_LETTER}
                </NavChip>
            </NavChipRow>
            <NavChipRow>
                {CYRILLIC_LABEL_LETTERS.map((letter) => (
                    <NavChip key={letter} href={hrefOf(letter)} active={current === letter} disabled={disabled}>
                        {letter}
                    </NavChip>
                ))}
            </NavChipRow>
        </NavChips>
    );
}
import {useTranslation} from 'react-i18next';
