import {SoftwareDetailsProvider, SoftwareForm, type SoftwareId, SoftwareFormSkeleton, softwarePath, useSoftwareDetails} from '@/entities/software';
import {DeleteSoftwareButton} from '@/features/software/delete';
import {EditSoftwareProvider} from '@/features/software/edit';
import {ScreenshotsEditorSection} from '@/features/software/screenshots';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';
import {Stack} from '@/shared/ui/stack';
import {SoftwareCategoriesField} from './SoftwareCategoriesField';

export interface SoftwareEditorProps {
    readonly id: SoftwareId;
    /** Called with the software once it is saved. */
    readonly onSaved: (id: SoftwareId) => void;
    /** Called once the software is deleted. */
    readonly onDeleted: () => void;
}

/** The page of editing one software: deleting next to its title, its form with its categories, and its screenshots below. */
export function SoftwareEditor(props: SoftwareEditorProps) {
    return (
        <SoftwareDetailsProvider key={props.id} id={props.id}>
            <LoadedEditor {...props}/>
        </SoftwareDetailsProvider>
    );
}

function LoadedEditor({id, onSaved, onDeleted}: SoftwareEditorProps) {
    const {t} = useTranslation();
    const query = useSoftwareDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={t('actions.editSoftware')}>
                <div data-testid="software-editor-skeleton"><SoftwareFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <PageLayout title={t('actions.editSoftware')} actions={<DeleteSoftwareButton software={query.data} onDeleted={onDeleted}/>}>
            <Stack gap="xl">
                <EditSoftwareProvider software={query.data} onSaved={() => {
                    onSaved(id);
                }}>
                    <SoftwareForm
                        submitLabel={t('actions.save')}
                        actions={<Button href={softwarePath(id)} color="secondary">{t('actions.cancel')}</Button>}
                        categories={<SoftwareCategoriesField/>}
                    />
                </EditSoftwareProvider>
                <ScreenshotsEditorSection softwareId={id}/>
            </Stack>
        </PageLayout>
    );
}
import {useTranslation} from 'react-i18next';
