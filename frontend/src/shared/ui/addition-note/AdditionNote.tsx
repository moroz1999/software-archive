import {useTranslation} from 'react-i18next';
import type {Addition} from '@/shared/api';
import {dayText} from '@/shared/lib/i18n';
import {Text} from '@/shared/ui/typography';

export interface AdditionNoteProps {
    readonly addition: Addition;
}

/** Who added an entry and on which day; only the day once the account of whoever added it is gone. */
export function AdditionNote({addition}: AdditionNoteProps) {
    const {t, i18n} = useTranslation();
    const day = dayText(addition.at, i18n.language);
    return (
        <Text variant="bodySm" tone="muted">
            {addition.by === null ? t('addition.on', {day}) : t('addition.byOn', {by: addition.by, day})}
        </Text>
    );
}
