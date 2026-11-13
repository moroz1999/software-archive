import {useTranslation} from 'react-i18next';
import {RegisterForm} from '@/features/user/register';
import {PageLayout} from '@/shared/ui/page-layout';

export function RegisterPage() {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('account.registration')}>
            <RegisterForm/>
        </PageLayout>
    );
}
