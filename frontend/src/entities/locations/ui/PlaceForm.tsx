import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {Form, FormActions, FormField, FormMessage} from '@/shared/ui/form';
import {Input} from '@/shared/ui/input';
import {Select} from '@/shared/ui/select';
import {useCountries} from '../model/countries-context';
import {type CoordinateProblem, PLACE_NAME_LANGUAGES} from '../model/place-form';
import {usePlaceForm} from '../model/place-form-context';

const COORDINATE_PROBLEM_KEYS: Readonly<Record<CoordinateProblem, string>> = {
    'latitude-range': 'geography.latitudeRange',
    'longitude-range': 'geography.longitudeRange',
    'pair': 'geography.coordinatesPair',
};

export interface PlaceFormProps {
    readonly submitLabel: string;
    /** More buttons before the submit button, such as Cancel. */
    readonly actions?: ReactNode;
}

/**
 * The fields of a country or a city inside a PlaceFormProvider: a name per language with the Russian one required,
 * the coordinates and, for a city, its country from the CountriesProvider around.
 */
export function PlaceForm({submitLabel, actions}: PlaceFormProps) {
    const {t} = useTranslation();
    const {kind, values, errors, countryRefusal, changeName, changeLatitude, changeLongitude, changeCountry, submit, busy, error} = usePlaceForm();
    const problem = (coordinate: CoordinateProblem | undefined) => coordinate === undefined ? undefined : t(COORDINATE_PROBLEM_KEYS[coordinate]);

    return (
        <Form onSubmit={submit}>
            {error !== undefined && <FormMessage>{error}</FormMessage>}
            {PLACE_NAME_LANGUAGES.map((language) => (
                <FormField
                    key={language}
                    label={t(`geography.nameIn.${language}`)}
                    required={language === 'ru'}
                    error={language === 'ru' && errors.nameRequired === true ? t('geography.nameRequired') : undefined}
                >
                    {(control) => <Input {...control} value={values.names[language]} onChange={changeName(language)} disabled={busy}/>}
                </FormField>
            ))}
            {kind === 'city' && <CountryField value={values.country} onChange={changeCountry} error={countryRefusal} disabled={busy}/>}
            <FormField label={t('geography.latitude')} error={problem(errors.latitude)}>
                {(control) => <Input {...control} value={values.latitude} onChange={changeLatitude} disabled={busy}/>}
            </FormField>
            <FormField label={t('geography.longitude')} error={problem(errors.longitude)}>
                {(control) => <Input {...control} value={values.longitude} onChange={changeLongitude} disabled={busy}/>}
            </FormField>
            <FormActions>
                {actions}
                <Button type="submit" loading={busy}>{submitLabel}</Button>
            </FormActions>
        </Form>
    );
}

function CountryField({value, onChange, error, disabled}: {
    readonly value: string;
    readonly onChange: (value: string) => void;
    readonly error: string | undefined;
    readonly disabled: boolean;
}) {
    const {t} = useTranslation();
    const countries = useCountries();
    return (
        <FormField label={t('geography.country')} error={error}>
            {(control) => (
                <Select
                    {...control}
                    value={value}
                    onChange={onChange}
                    options={(countries.data ?? []).map((country) => ({value: country.id, label: country.name}))}
                    placeholder={t('location.noCountry')}
                    disabled={disabled || countries.data === undefined}
                />
            )}
        </FormField>
    );
}
