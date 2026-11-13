import {useTranslation} from 'react-i18next';
import {useSearchParams} from 'react-router';
import {ConfirmEmail} from '@/features/user/confirm-email';
import {PageLayout} from '@/shared/ui/page-layout';

/** Where a confirmation link leads: the account of its `token` is confirmed on opening. */
export function VerifyEmailPage() {
    const {t} = useTranslation();
    const [params] = useSearchParams();
    return (
        <PageLayout title={t('account.confirmation')}>
            <ConfirmEmail token={params.get('token')}/>
        </PageLayout>
    );
}
