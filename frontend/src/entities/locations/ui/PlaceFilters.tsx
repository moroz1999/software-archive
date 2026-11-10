import {Field} from '@/shared/ui/field';
import {Inline} from '@/shared/ui/inline';
import {Select} from '@/shared/ui/select';
import {type City, type CityId, type Country, type CountryId, toCityId, toCountryId} from '../model/geo';
import styles from './PlaceFilters.module.css';

export interface PlaceFiltersProps {
    readonly country: CountryId | null;
    readonly city: CityId | null;
    /** Undefined while they load or when they failed: the filter is locked. */
    readonly countries: readonly Country[] | undefined;
    /** The cities of the chosen country; undefined while none is chosen or while they load. */
    readonly cities: readonly City[] | undefined;
    readonly disabled: boolean;
    /** Another country forgets the city. */
    readonly onChange: (country: CountryId | null, city: CityId | null) => void;
}

/** Country and City to narrow a list by place: City offers the cities of the chosen country. */
export function PlaceFilters({country, city, countries, cities, disabled, onChange}: PlaceFiltersProps) {
    const {t} = useTranslation();
    const countryOptions = (countries ?? []).map((item) => ({value: item.id, label: item.name}));
    const cityOptions = (cities ?? []).map((item) => ({value: item.id, label: item.name}));
    return (
        <Inline gap="md" wrap>
            <Field label={t('filters.country')} className={styles.filter}>
                {(control) => (
                    <Select
                        id={control.id}
                        value={country ?? ''}
                        onChange={(value) => {
                            onChange(value === '' ? null : toCountryId(value), null);
                        }}
                        options={countryOptions}
                        placeholder={t('filters.anyCountry')}
                        disabled={disabled || countries === undefined}
                    />
                )}
            </Field>
            <Field label={t('filters.city')} className={styles.filter}>
                {(control) => (
                    <Select
                        id={control.id}
                        value={city ?? ''}
                        onChange={(value) => {
                            onChange(country, value === '' ? null : toCityId(value));
                        }}
                        options={cityOptions}
                        placeholder={t('filters.anyCity')}
                        disabled={disabled || cities === undefined}
                    />
                )}
            </Field>
        </Inline>
    );
}
import {useTranslation} from 'react-i18next';
