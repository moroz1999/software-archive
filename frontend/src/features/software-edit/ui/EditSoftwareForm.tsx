import type {ReactNode} from 'react';
import {type SoftwareDetails, type SoftwareDraft, SoftwareForm, toSoftwareFormValues, updateSoftware} from '../../../entities/software';
import {businessErrorMessage, useAction} from '../../../shared/api';
import {editSoftwareMessages} from '../config/messages';

export interface EditSoftwareFormProps {
    readonly software: SoftwareDetails;
    readonly onSaved: () => void;
    /** More buttons next to Save. */
    readonly actions?: ReactNode;
}

/** Saves changes of existing software. */
export function EditSoftwareForm({software, onSaved, actions}: EditSoftwareFormProps) {
    const {state, run} = useAction((draft: SoftwareDraft) => updateSoftware(software.id, draft), () => {
        onSaved();
    });

    return (
        <SoftwareForm
            submitLabel="Save"
            initial={toSoftwareFormValues(software)}
            onSubmit={run}
            busy={state.status === 'running'}
            error={state.status === 'business-error' ? businessErrorMessage(state.error, editSoftwareMessages) : undefined}
            actions={actions}
        />
    );
}
