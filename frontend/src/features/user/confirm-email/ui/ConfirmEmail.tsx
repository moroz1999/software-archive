import {useTranslation} from 'react-i18next';
import {REGISTER_PATH} from '@/entities/users';
import {HOME_PATH} from '@/shared/config';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useConfirmEmail} from '../model/confirm-email-context';
import {ConfirmEmailProvider} from '../model/ConfirmEmailProvider';

export interface ConfirmEmailProps {
    /** The token of the link; null when the link has none. */
    readonly token: string | null;
}

/** Confirms the account of a link as soon as it opens, and tells how it went. */
export function ConfirmEmail({token}: ConfirmEmailProps) {
    return (
        <ConfirmEmailProvider token={token}>
            <Outcome/>
        </ConfirmEmailProvider>
    );
}

function Outcome() {
    const {t} = useTranslation();
    const outcome = useConfirmEmail();

    switch (outcome) {
        case 'confirming':
        case 'failed':
            return <Text role="status" tone="muted">{t('account.confirming')}</Text>;
        case 'confirmed':
            return (
                <Stack gap="md">
                    <Text role="status">{t('account.confirmed')}</Text>
                    <Inline><Button href={HOME_PATH}>{t('account.home')}</Button></Inline>
                </Stack>
            );
        case 'expired':
            return (
                <Stack gap="md">
                    <Text role="alert" tone="danger">{t('account.confirmationExpired')}</Text>
                    <Inline><Button href={REGISTER_PATH}>{t('account.registerAgain')}</Button></Inline>
                </Stack>
            );
        case 'invalid':
            return (
                <Stack gap="md">
                    <Text role="alert" tone="danger">{t('account.confirmationInvalid')}</Text>
                    <Inline><Button href={REGISTER_PATH}>{t('account.registerAgain')}</Button></Inline>
                </Stack>
            );
    }
}
