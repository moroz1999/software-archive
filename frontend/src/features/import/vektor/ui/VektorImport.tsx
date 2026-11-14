import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {useVektorImport} from '../model/vektor-import-context';
import {VektorImportProvider} from '../model/VektorImportProvider';
import {ImportRunReport} from './ImportRunReport';

/** Starts the import of vektor-06c.ru, whole or one section of it, and reports what the import started last did. */
export function VektorImport() {
    return (
        <VektorImportProvider>
            <Stack gap="xl">
                <VektorImportForm/>
                <ImportRunReport/>
            </Stack>
        </VektorImportProvider>
    );
}

function VektorImportForm() {
    const {t} = useTranslation();
    const {sections, section, choose, start, starting, started, error} = useVektorImport();

    return (
        <Form onSubmit={start}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            {started && <FormMessage variant="success">{t('imports.started')}</FormMessage>}
            <FormSection title={t('imports.vektor')}>
                <FormField label={t('imports.section')}>
                    {(control) => (
                        <Select
                            {...control}
                            value={section}
                            onChange={choose}
                            options={(sections.data ?? []).map((code) => ({value: code, label: code}))}
                            placeholder={t('imports.wholeSite')}
                            disabled={starting || sections.data === undefined}
                        />
                    )}
                </FormField>
            </FormSection>
            <FormActions>
                <Button type="submit" loading={starting} disabled={sections.data === undefined}>{t('imports.start')}</Button>
            </FormActions>
        </Form>
    );
}
