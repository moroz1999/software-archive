import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage, FormSection} from '@/shared/ui/form';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {IMPORT_SOURCES, isImportSource} from '../model/import-run';
import {useSourceImport} from '../model/source-import-context';
import {SourceImportProvider} from '../model/SourceImportProvider';
import {ImportRunReport} from './ImportRunReport';

/**
 * Starts the import of a source archive — vektor-06c.ru whole or one section of it, or the card index "Базис" — and
 * reports what the import started last did.
 */
export function SourceImport() {
    return (
        <SourceImportProvider>
            <Stack gap="xl">
                <SourceImportForm/>
                <ImportRunReport/>
            </Stack>
        </SourceImportProvider>
    );
}

function SourceImportForm() {
    const {t} = useTranslation();
    const {sections, source, chooseSource, section, choose, start, starting, started, error} = useSourceImport();
    // Only vektor-06c.ru has sections; its list is read before a start either way.
    const vektor = source === 'vektor';

    return (
        <Form onSubmit={start}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            {started && <FormMessage variant="success">{t('imports.started')}</FormMessage>}
            <FormSection title={t('imports.whatToImport')}>
                <FormField label={t('imports.source')}>
                    {(control) => (
                        <Select
                            {...control}
                            value={source}
                            onChange={(raw) => {
                                if (isImportSource(raw)) {
                                    chooseSource(raw);
                                }
                            }}
                            options={IMPORT_SOURCES.map((code) => ({value: code, label: t(`imports.sources.${code}`)}))}
                            disabled={starting}
                        />
                    )}
                </FormField>
                {vektor && <FormField label={t('imports.section')}>
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
                </FormField>}
            </FormSection>
            <FormActions>
                <Button type="submit" loading={starting} disabled={sections.data === undefined}>{t('imports.start')}</Button>
            </FormActions>
        </Form>
    );
}
