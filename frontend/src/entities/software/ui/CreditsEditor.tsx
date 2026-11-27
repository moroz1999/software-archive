import {useTranslation} from 'react-i18next';
import {creditRoleValues} from '@/shared/api';
import {MemberRoleEditor} from '@/shared/ui/member-role-editor';
import {fetchSoftwareParticipants} from '../api/software-api';
import type {CreditValue} from '../model/software-form';

export interface CreditsEditorProps {
    readonly value: readonly CreditValue[];
    readonly onChange: (credits: readonly CreditValue[]) => void;
    readonly disabled: boolean;
    /** Why the archive refused the credits. */
    readonly error: string | undefined;
}

/** People, groups and the labels of either credited on software or a release, each with their roles. */
export function CreditsEditor({value, onChange, disabled, error}: CreditsEditorProps) {
    const {t} = useTranslation();
    return (
        <MemberRoleEditor
            label={t('fields.participant')}
            value={value}
            onChange={onChange}
            search={fetchSoftwareParticipants}
            roles={creditRoleValues.map((code) => ({code, title: t(`roles.software.${code}`)}))}
            disabled={disabled}
            error={error}
        />
    );
}
