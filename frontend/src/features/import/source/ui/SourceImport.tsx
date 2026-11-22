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
 * Starts the import of a source archive — whole or one section or category — and reports what the import started last
 * did.
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
    const {sections, basisCategories, source, chooseSource, section, choose, start, starting, started, error} = useSourceImport();
    const vektor = source === 'vektor';
    const choices = vektor ? sections : basisCategories;

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
                <FormField label={t(vektor ? 'imports.section' : 'imports.category')}>
                    {(control) => (
                        <Select
                            {...control}
                            value={section}
                            onChange={choose}
                            options={(choices.data ?? []).map((code) => ({value: code, label: code}))}
                            placeholder={t(vektor ? 'imports.wholeSite' : 'imports.wholeCardIndex')}
                            disabled={starting || choices.data === undefined}
                        />
                    )}
                </FormField>
            </FormSection>
            <FormActions>
                <Button type="submit" loading={starting} disabled={choices.data === undefined}>{t('imports.start')}</Button>
            </FormActions>
        </Form>
    );
}
