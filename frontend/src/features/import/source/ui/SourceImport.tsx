import {useTranslation} from 'react-i18next';
import {bkCatalogSectionValues} from '@/shared/api';
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
    const {source, chooseSource, start, starting, started, error} = useSourceImport();
    const {label, options, placeholder, ready} = useSectionChoice();

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
                <SectionField label={label} options={options} placeholder={placeholder} disabled={starting || !ready}/>
            </FormSection>
            <FormActions>
                <Button type="submit" loading={starting} disabled={!ready}>{t('imports.start')}</Button>
            </FormActions>
        </Form>
    );
}

interface SectionChoice {
    readonly label: string;
    readonly options: readonly {readonly value: string; readonly label: string}[];
    readonly placeholder: string;
    /** Whether the choices are there to start from: the sections of a site are read from the archive first. */
    readonly ready: boolean;
}

/** What part of the chosen source can be imported: the sections or categories it has, or the whole of it. */
function useSectionChoice(): SectionChoice {
    const {t} = useTranslation();
    const {sections, basisCategories, source} = useSourceImport();
    switch (source) {
        case 'vektor':
            return {
                label: t('imports.section'),
                options: (sections.data ?? []).map((code) => ({value: code, label: code})),
                placeholder: t('imports.wholeSite'),
                ready: sections.data !== undefined,
            };
        case 'basis':
            return {
                label: t('imports.category'),
                options: (basisCategories.data ?? []).map((code) => ({value: code, label: code})),
                placeholder: t('imports.wholeCardIndex'),
                ready: basisCategories.data !== undefined,
            };
        case 'bkCatalog':
            return {
                label: t('imports.section'),
                options: bkCatalogSectionValues.map((code) => ({value: code, label: t(`imports.bkCatalogSections.${code}`)})),
                placeholder: t('imports.wholeCatalog'),
                ready: true,
            };
    }
}

interface SectionFieldProps {
    readonly label: string;
    readonly options: SectionChoice['options'];
    readonly placeholder: string;
    readonly disabled: boolean;
}

function SectionField({label, options, placeholder, disabled}: SectionFieldProps) {
    const {section, choose} = useSourceImport();
    return (
        <FormField label={label}>
            {(control) => (
                <Select {...control} value={section} onChange={choose} options={options} placeholder={placeholder} disabled={disabled}/>
            )}
        </FormField>
    );
}
