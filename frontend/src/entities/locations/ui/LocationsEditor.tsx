import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {Button} from '@/shared/ui/button';
import {FieldGroup} from '@/shared/ui/field-group';
import {Inline} from '@/shared/ui/inline';
import {ItemRow} from '@/shared/ui/item-row';
import {Radio} from '@/shared/ui/radio';
import {Select} from '@/shared/ui/select';
import {Text} from '@/shared/ui/typography';
import {useCities} from '../model/cities-context';
import {CitiesProvider} from '../model/CitiesProvider';
import {useCountries} from '../model/countries-context';
import {CountriesProvider} from '../model/CountriesProvider';
import {parseCountryId} from '../model/geo';
import type {LocationValue} from '../model/location-value';

/** The locations with a primary one: the first one when none is. */
function withPrimary(locations: readonly LocationValue[]): readonly LocationValue[] {
    return locations.some((location) => location.primary) ? locations : locations.map((location, i) => ({...location, primary: i === 0}));
}

export interface LocationsEditorProps {
    readonly value: readonly LocationValue[];
    readonly onChange: (value: readonly LocationValue[]) => void;
    /** What is wrong with each location, by its row. */
    readonly rowErrors?: readonly (string | undefined)[] | undefined;
    /** What is wrong with the locations as a whole. */
    readonly error?: string | undefined;
    readonly disabled?: boolean;
}

/**
 * Where someone is located: a country, or a city of it, in each row; exactly one, chosen by its radio, is the primary one — the
 * first location added becomes it, and taking it away passes it to the first location left.
 */
export function LocationsEditor(props: LocationsEditorProps) {
    return (
        <CountriesProvider>
            <Locations {...props}/>
        </CountriesProvider>
    );
}

function Locations({value, onChange, rowErrors = [], error, disabled = false}: LocationsEditorProps) {
    const {t} = useTranslation();
    const groupId = useId();

    function update(index: number, location: LocationValue) {
        onChange(value.map((current, i) => (i === index ? location : current)));
    }

    function mark(index: number) {
        onChange(value.map((location, i) => ({...location, primary: i === index})));
    }

    return (
        <FieldGroup legend={t('location.locations')} error={error}>
            {value.map((location, index) => (
                <CitiesProvider key={index} country={parseCountryId(location.country)}>
                    <LocationRow
                        index={index}
                        location={location}
                        error={rowErrors[index]}
                        disabled={disabled}
                        onChange={(changed) => {
                            update(index, changed);
                        }}
                        primaryGroup={`${groupId}-primary`}
                        onMark={() => {
                            mark(index);
                        }}
                        onRemove={() => {
                            onChange(withPrimary(value.filter((_, i) => i !== index)));
                        }}
                    />
                </CitiesProvider>
            ))}
            <Inline gap="sm">
                <Button
                    color="outlined"
                    size="sm"
                    onClick={() => {
                        onChange([...value, {country: '', city: '', primary: value.length === 0}]);
                    }}
                    disabled={disabled}
                >
                    {t('location.add')}
                </Button>
            </Inline>
        </FieldGroup>
    );
}

interface LocationRowProps {
    readonly index: number;
    readonly location: LocationValue;
    readonly error: string | undefined;
    readonly disabled: boolean;
    /** The radio group of the primary location. */
    readonly primaryGroup: string;
    readonly onChange: (location: LocationValue) => void;
    readonly onMark: () => void;
    readonly onRemove: () => void;
}

/** One location: City offers the cities of the chosen country; another country forgets the city. */
function LocationRow({index, location, error, disabled, primaryGroup, onChange, onMark, onRemove}: LocationRowProps) {
    const {t} = useTranslation();
    const id = useId();
    const countries = useCountries();
    const cities = useCities();
    const n = index + 1;
    const errorId = `${id}-error`;
    return (
        <ItemRow
            columns={2}
            trailing={(
                <>
                    <Radio label={t('location.primary')} name={primaryGroup} checked={location.primary} onChoose={onMark} disabled={disabled}/>
                    <Button color="transparent" size="sm" square ariaLabel={t('location.remove', {n})} onClick={onRemove} disabled={disabled}>
                        ×
                    </Button>
                </>
            )}
            error={error !== undefined && <Text as="span" id={errorId} variant="caption" tone="danger">{error}</Text>}
        >
            <Select
                id={`${id}-country`}
                ariaLabel={t('location.country', {n})}
                value={location.country}
                onChange={(country) => {
                    onChange({...location, country, city: ''});
                }}
                options={(countries.data ?? []).map((country) => ({value: country.id, label: country.name}))}
                placeholder={t('location.chooseCountry')}
                invalid={error !== undefined}
                describedBy={error === undefined ? undefined : errorId}
                disabled={disabled || countries.data === undefined}
            />
            <Select
                id={`${id}-city`}
                ariaLabel={t('location.city', {n})}
                value={location.city}
                onChange={(city) => {
                    onChange({...location, city});
                }}
                options={(cities.data ?? []).map((city) => ({value: city.id, label: city.name}))}
                placeholder={t('location.noCity')}
                disabled={disabled || cities.data === undefined}
            />
        </ItemRow>
    );
}
