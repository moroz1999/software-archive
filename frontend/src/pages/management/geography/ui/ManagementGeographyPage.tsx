import {useTranslation} from 'react-i18next';
import {CitiesOfNoCountry, CitiesOfNoCountryProvider, CITY_NEW_PATH, CountriesProvider, COUNTRY_NEW_PATH, GeographyProvider, GeographyTree} from '@/entities/locations';
import {PutCityInCountry} from '@/features/geography/put-in-country';
import {Stack} from '@/shared/ui/stack';
import {ManagementTabs} from '@/features/user/management-navigation';
import {Button} from '@/shared/ui/button';
import {Inline} from '@/shared/ui/inline';
import {PageLayout} from '@/shared/ui/page-layout';

export function ManagementGeographyPage() {
    const {t} = useTranslation();
    return (
        <PageLayout
            title={t('pages.geography')}
            actions={(
                <Inline gap="sm">
                    <Button href={COUNTRY_NEW_PATH}>{t('actions.addCountry')}</Button>
                    <Button href={CITY_NEW_PATH} color="secondary">{t('actions.addCity')}</Button>
                </Inline>
            )}
        >
            <ManagementTabs/>
            <Stack gap="lg">
                <CountriesProvider>
                    <CitiesOfNoCountryProvider>
                        <CitiesOfNoCountry action={(city) => <PutCityInCountry city={city}/>}/>
                    </CitiesOfNoCountryProvider>
                </CountriesProvider>
                <GeographyProvider>
                    <GeographyTree/>
                </GeographyProvider>
            </Stack>
        </PageLayout>
    );
}
