import {SoftwareDetailsProvider, SoftwareForm, type SoftwareId, SoftwareFormSkeleton, useSoftwareDetails} from '@/entities/software';
import {DeleteSoftwareButton} from '@/features/software-delete';
import {EditSoftwareProvider} from '@/features/software-edit';
import {useSkeleton} from '@/shared/lib/skeleton';

export interface SoftwareEditorProps {
    readonly id: SoftwareId;
    /** Called with the software once it is saved. */
    readonly onSaved: (id: SoftwareId) => void;
    /** Called once the software is deleted. */
    readonly onDeleted: () => void;
}

/** Editing of one software: its form, saving and deleting. */
export function SoftwareEditor(props: SoftwareEditorProps) {
    return (
        <SoftwareDetailsProvider key={props.id} id={props.id}>
            <LoadedEditor {...props}/>
        </SoftwareDetailsProvider>
    );
}

function LoadedEditor({id, onSaved, onDeleted}: SoftwareEditorProps) {
    const query = useSoftwareDetails();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return <div data-testid="software-editor-skeleton"><SoftwareFormSkeleton/></div>;
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    return (
        <EditSoftwareProvider software={query.data} onSaved={() => {
            onSaved(id);
        }}>
            <SoftwareForm submitLabel="Save" actions={<DeleteSoftwareButton software={query.data} onDeleted={onDeleted}/>}/>
        </EditSoftwareProvider>
    );
}
