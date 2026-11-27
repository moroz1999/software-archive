import {useId, useState} from 'react';
import {useTranslation} from 'react-i18next';
import {CitiesProvider, type PlaceDetails, placeName, useCities, useCountries} from '@/entities/locations';
import {parseUiLanguage} from '@/shared/lib/i18n';
import {Button} from '@/shared/ui/button';
import {Dialog} from '@/shared/ui/dialog';
import {Select} from '@/shared/ui/select';
import {Stack} from '@/shared/ui/stack';
import {Text} from '@/shared/ui/typography';
import {useMergePlace} from '../model/merge-place-context';
import {MergePlaceProvider} from '../model/MergePlaceProvider';

export interface MergePlaceButtonProps {
    readonly place: PlaceDetails;
    readonly onMerged: () => void;
}

/**
 * Merges a duplicate country or city into another one chosen: a country from the CountriesProvider around, a city
 * from the cities of a country chosen there. What it is merged into stays as it is.
 */
export function MergePlaceButton({place, onMerged}: MergePlaceButtonProps) {
    return (
        <MergePlaceProvider place={place} onMerged={onMerged}>
            <MergeWithDialog/>
        </MergePlaceProvider>
    );
}

function MergeWithDialog() {
    const {t, i18n} = useTranslation();
    const {place, merge, merging, error, reset} = useMergePlace();
    // Whether the dialog is open and what is chosen in it is its own UI state until it is sent.
    const [open, setOpen] = useState(false);
    const [country, setCountry] = useState('');
    const [city, setCity] = useState('');
    const isCity = place.kind === 'city';
    const into = isCity ? city : country;
    const title = t(isCity ? 'geography.mergeCity' : 'geography.mergeCountry');
    const name = placeName(place.names, parseUiLanguage(i18n.language) ?? 'en');

    function close() {
        setOpen(false);
        setCountry('');
        setCity('');
        reset();
    }

    return (
        <>
            <Button color="secondary" onClick={() => {
                setOpen(true);
            }}>
                {t('geography.mergeInto')}
            </Button>
            <Dialog
                open={open}
                title={title}
                onClose={merging ? noop : close}
                actions={(
                    <>
                        <Button color="secondary" disabled={merging} onClick={close}>{t('actions.cancel')}</Button>
                        <Button
                            disabled={into === ''}
                            loading={merging}
                            onClick={() => {
                                merge(into);
                            }}
                        >
                            {t('geography.merge')}
                        </Button>
                    </>
                )}
            >
                <Stack gap="md">
                    <Text>{t(isCity ? 'geography.mergeCityQuestion' : 'geography.mergeCountryQuestion', {name})}</Text>
                    <CountryChoice
                        value={country}
                        onChange={(chosen) => {
                            setCountry(chosen);
                            setCity('');
                            reset();
                        }}
                        except={isCity ? undefined : place.id}
                    />
                    {isCity && (
                        <CitiesProvider country={country === '' ? null : country}>
                            <CityChoice
                                value={city}
                                onChange={(chosen) => {
                                    setCity(chosen);
                                    reset();
                                }}
                                except={place.id}
                            />
                        </CitiesProvider>
                    )}
                    {error !== undefined && <Text role="alert" tone="danger">{error}</Text>}
                </Stack>
            </Dialog>
        </>
    );
}

interface ChoiceProps {
    readonly value: string;
    readonly onChange: (value: string) => void;
    /** The place being merged, never offered to merge into. */
    readonly except: string | undefined;
}

function CountryChoice({value, onChange, except}: ChoiceProps) {
    const {t} = useTranslation();
    const id = useId();
    const countries = useCountries();
    return (
        <Select
            id={id}
            value={value}
            onChange={onChange}
            options={(countries.data ?? []).filter((option) => option.id !== except).map((option) => ({value: option.id, label: option.name}))}
            placeholder={t('geography.chooseCountry')}
            ariaLabel={t('geography.country')}
            disabled={countries.data === undefined}
        />
    );
}

function CityChoice({value, onChange, except}: ChoiceProps) {
    const {t} = useTranslation();
    const id = useId();
    const cities = useCities();
    return (
        <Select
            id={id}
            value={value}
            onChange={onChange}
            options={(cities.data ?? []).filter((option) => option.id !== except).map((option) => ({value: option.id, label: option.name}))}
            placeholder={t('geography.chooseCity')}
            ariaLabel={t('geography.city')}
            disabled={cities.data === undefined}
        />
    );
}

function noop() {
    // The dialog stays open while the place is being merged.
}
