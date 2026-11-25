import {useTranslation} from 'react-i18next';
import {MemberRoleEditor, type MemberRoleValue} from '@/shared/ui/member-role-editor';
import {fetchSoftwareParticipants} from '../api/software-api';

/** Developing and publishing come first: they are what software is known by (docs/domain/software.md#credits). */
const SOFTWARE_ROLES = [
    'unknown', 'developer', 'publisher', 'original_author', 'code', 'release', 'adaptation', 'restoring', 'music', 'sfx', 'support', 'testing', 'graphics',
    'loading_screen', 'intro_code', 'intro_graphics', 'intro_music', 'organizing', 'direction', '3dmodels', 'design',
    'logo', 'font', 'ascii', 'illustrating', 'tools', 'localization', 'concept', 'gamedesign', 'leveldesign', 'story',
    'text', 'editing', 'translation', 'video', 'guest',
] as const;

export interface CreditsEditorProps {
    readonly value: readonly MemberRoleValue[];
    readonly onChange: (credits: readonly MemberRoleValue[]) => void;
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
            roles={SOFTWARE_ROLES.map((code) => ({code, title: t(`roles.software.${code}`)}))}
            disabled={disabled}
            error={error}
        />
    );
}
