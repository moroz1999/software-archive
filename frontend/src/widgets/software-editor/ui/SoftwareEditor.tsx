import {parseSoftwareId, type SoftwareId, SoftwareFormSkeleton} from '../../../entities/software';
import {DeleteSoftwareButton} from '../../../features/software-delete';
import {EditSoftwareForm} from '../../../features/software-edit';
import {businessErrorMessage, useRequest} from '../../../shared/api';
import {ErrorState, Text} from '../../../shared/ui';
import {softwareDetailsRequest} from '../api/software-details-request';
import {softwareEditorMessages} from '../config/messages';

export interface SoftwareEditorProps {
    /** The id from the URL, not yet checked. */
    readonly id: string | undefined;
    /** Called once the software is saved or deleted. */
    readonly onDone: () => void;
}

const NOT_FOUND = softwareEditorMessages['software.not-found'];

/** Editing of one software: its form, saving and deleting. */
export function SoftwareEditor({id, onDone}: SoftwareEditorProps) {
    const softwareId = parseSoftwareId(id);
    if (softwareId === null) {
        return <Text tone="muted">{NOT_FOUND}</Text>;
    }
    return <LoadedEditor key={softwareId} id={softwareId} onDone={onDone}/>;
}

function LoadedEditor({id, onDone}: {readonly id: SoftwareId; readonly onDone: () => void}) {
    const {state, reload} = useRequest(softwareDetailsRequest.for(id));

    switch (state.status) {
        case 'loading':
            return <div data-testid="software-editor-skeleton"><SoftwareFormSkeleton/></div>;
        case 'system-error':
            // Shown by the system error dialog (docs/error-handling.md).
            return null;
        case 'business-error':
            if (state.error.status === 404) {
                return <Text tone="muted">{businessErrorMessage(state.error, softwareEditorMessages)}</Text>;
            }
            return <ErrorState message={businessErrorMessage(state.error, softwareEditorMessages)} onRetry={reload}/>;
        case 'success':
            return (
                <EditSoftwareForm
                    software={state.data}
                    onSaved={onDone}
                    actions={<DeleteSoftwareButton software={state.data} onDeleted={onDone}/>}
                />
            );
    }
}
