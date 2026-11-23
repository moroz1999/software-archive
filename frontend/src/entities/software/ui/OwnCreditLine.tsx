import {useTranslation} from 'react-i18next';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Text} from '@/shared/ui/typography';
import type {OwnCredit} from '../model/credited-software';

/** The roles of one credit, an unknown one left out, after the label it was under when that is not the main name. */
export function OwnCreditLine({credit}: {readonly credit: OwnCredit}) {
    const {t} = useTranslation();
    const roles = credit.roles.filter((role) => role !== 'unknown');
    if (credit.as === null && roles.length === 0) {
        return null;
    }
    return (
        <Inline gap="xs" align="center" wrap>
            {credit.as !== null && <Text variant="bodySm" tone="muted">{t('software.creditedAs', {name: credit.as})}</Text>}
            {roles.map((role) => <Chip key={role} size="sm">{t(`roles.software.${role}`)}</Chip>)}
        </Inline>
    );
}
