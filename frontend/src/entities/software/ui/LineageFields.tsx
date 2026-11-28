import {useTranslation} from 'react-i18next';
import {lineageKindValues, lineageReliabilityValues, lineageReworkValues} from '@/shared/api';
import {Checkbox} from '@/shared/ui/checkbox';
import {FormField, FormFieldset} from '@/shared/ui/form';
import {Inline} from '@/shared/ui/inline';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {LINEAGE_SOURCE_TOO_LONG_KEY} from '../config/messages';
import {useSoftwareForm} from '../model/software-form-context';
import type {LineageValues} from '../model/software-form';

const UNKNOWN: LineageValues = {kind: '', reliability: '', rework: [], source: ''};

/**
 * Where the work comes from (docs/domain/software.md#lineage). The reliability and the rework tell of a kind: they stay
 * locked while the kind is unknown, and making it unknown clears them.
 */
export function LineageFields() {
    const {t} = useTranslation();
    const {values, errors, changeLineage, busy} = useSoftwareForm();
    const lineage = values.lineage ?? UNKNOWN;
    const kindUnknown = lineage.kind === '';

    function changeKind(kind: string) {
        changeLineage(kind === '' ? {...lineage, kind, reliability: '', rework: []} : {...lineage, kind});
    }

    function toggleRework(code: string, checked: boolean) {
        changeLineage({...lineage, rework: checked ? [...lineage.rework, code] : lineage.rework.filter((known) => known !== code)});
    }

    return (
        <>
            <FormField label={t('fields.lineageKind')}>
                {(control) => (
                    <Select
                        {...control}
                        value={lineage.kind}
                        onChange={changeKind}
                        options={lineageKindValues.map((kind) => ({value: kind, label: t(`software.lineage.kinds.${kind}`)}))}
                        placeholder={t('software.lineage.unknown')}
                        disabled={busy}
                    />
                )}
            </FormField>
            <FormField label={t('fields.lineageReliability')}>
                {(control) => (
                    <Select
                        {...control}
                        value={lineage.reliability}
                        onChange={(reliability) => {
                            changeLineage({...lineage, reliability});
                        }}
                        options={lineageReliabilityValues.map((reliability) => ({value: reliability, label: t(`software.lineage.reliabilities.${reliability}`)}))}
                        placeholder={t('software.lineage.unknown')}
                        disabled={busy || kindUnknown}
                    />
                )}
            </FormField>
            <FormFieldset legend={t('fields.lineageRework')} fullWidth>
                <Inline gap="md" wrap>
                    {lineageReworkValues.map((code) => (
                        <Checkbox
                            key={code}
                            label={t(`software.lineage.reworks.${code}`)}
                            checked={lineage.rework.includes(code)}
                            onChange={(checked) => {
                                toggleRework(code, checked);
                            }}
                            disabled={busy || kindUnknown}
                        />
                    ))}
                </Inline>
            </FormFieldset>
            <FormField label={t('fields.lineageSource')} fullWidth error={errors.lineageSource === undefined ? undefined : t(LINEAGE_SOURCE_TOO_LONG_KEY)}>
                {(control) => (
                    <Input
                        {...control}
                        value={lineage.source}
                        onChange={(source) => {
                            changeLineage({...lineage, source});
                        }}
                        disabled={busy}
                    />
                )}
            </FormField>
        </>
    );
}
