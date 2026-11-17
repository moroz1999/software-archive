import {useTranslation} from 'react-i18next';
import {LabelDetailsProvider, type LabelId, labelPath, useLabelDetails} from '@/entities/labels';
import {DeleteLabelButton} from '@/features/label/delete';
import {EditLabelForm, EditLabelFormSkeleton, EditLabelProvider} from '@/features/label/label-edit';
import {useSkeleton} from '@/shared/lib/loading';
import {Button} from '@/shared/ui/button';
import {PageLayout} from '@/shared/ui/page-layout';

export interface LabelEditorProps {
    readonly id: LabelId;
    /** Called once the label is saved. */
    readonly onSaved: () => void;
    /** Called once the label is deleted. */
    readonly onDeleted: () => void;
}

/** The page of editing one label: Delete next to the title, the form with Cancel and Save under it. */
export function LabelEditor(props: LabelEditorProps) {
    return (
        <LabelDetailsProvider key={props.id} id={props.id}>
            <LoadedEditor {...props}/>
        </LabelDetailsProvider>
    );
}

function LoadedEditor({id, onSaved, onDeleted}: LabelEditorProps) {
    const {t} = useTranslation();
    const query = useLabelDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <PageLayout title={t('actions.editLabel')}>
                <div data-testid="label-editor-skeleton"><EditLabelFormSkeleton/></div>
            </PageLayout>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    const label = query.data;
    return (
        <PageLayout title={t('actions.editLabel')} actions={<DeleteLabelButton label={{id: label.id, name: label.name}} onDeleted={onDeleted}/>}>
            <EditLabelProvider label={label} onSaved={onSaved}>
                <EditLabelForm actions={<Button href={labelPath(id)} color="secondary">{t('actions.cancel')}</Button>}/>
            </EditLabelProvider>
        </PageLayout>
    );
}
