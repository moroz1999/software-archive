import {createSoftware, type SoftwareId, SoftwareForm} from '../../../entities/software';
import {businessErrorMessage, useAction} from '../../../shared/api';
import {createSoftwareMessages} from '../config/messages';

export interface CreateSoftwareFormProps {
    readonly onCreated: (id: SoftwareId) => void;
}

/** Adds software to the archive. */
export function CreateSoftwareForm({onCreated}: CreateSoftwareFormProps) {
    const {state, run} = useAction(createSoftware, onCreated);

    return (
        <SoftwareForm
            submitLabel="Add software"
            onSubmit={run}
            busy={state.status === 'running'}
            error={state.status === 'business-error' ? businessErrorMessage(state.error, createSoftwareMessages) : undefined}
        />
    );
}
