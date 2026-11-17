import {useTranslation} from 'react-i18next';
import {AUTHORS_PATH} from '@/entities/authors';
import type {LabelId} from '@/entities/labels';
import {CreateLabelForm, CreateLabelProvider} from '@/features/label/create';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export interface LabelCreatorProps {
    /** Called with the id of the label once it is added. */
    readonly onCreated: (id: LabelId) => void;
}

/** The page of adding a label of nobody: the form with Cancel back to the author catalog. */
export function LabelCreator({onCreated}: LabelCreatorProps) {
    const {t} = useTranslation();
    return (
        <PageLayout title={t('actions.addLabel')}>
            <CreateLabelProvider onCreated={onCreated}>
                <CreateLabelForm actions={<Button href={AUTHORS_PATH} color="secondary">{t('actions.cancel')}</Button>}/>
            </CreateLabelProvider>
        </PageLayout>
    );
}
