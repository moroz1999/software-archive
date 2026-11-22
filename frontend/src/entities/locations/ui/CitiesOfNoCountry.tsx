import type {ReactNode} from 'react';
import {useTranslation} from 'react-i18next';
import {useSkeleton} from '@/shared/lib/loading';
import {Panel} from '@/shared/ui/panel';
import {Skeleton} from '@/shared/ui/skeleton';
import {Table, TableBody, TableCell, TableHead, TableHeaderCell, TableRow} from '@/shared/ui/table';
import {Text, TextLink} from '@/shared/ui/typography';
import {cityEditPath} from '../config/paths';
import type {GeographyCity} from '../model/geography';
import {useCitiesOfNoCountry} from '../model/geography-context';
import styles from './CitiesOfNoCountry.module.css';

const SKELETON_ROWS = [1, 2, 3];

export interface CitiesOfNoCountryProps {
    /** The choice of a country to put the city in. */
    readonly country: (city: GeographyCity) => ReactNode;
    /** What else can be done with the city: making it a country. */
    readonly action: (city: GeographyCity) => ReactNode;
}

/**
 * The cities of no known country from the CitiesOfNoCountryProvider around, a table of each leading to its editing,
 * how many places are in it, its country to choose and what else can be done with it.
 */
export function CitiesOfNoCountry(props: CitiesOfNoCountryProps) {
    const {t} = useTranslation();
    return (
        <Panel as="section" title={t('geography.citiesOfNoCountry')} titleLevel={2}>
            <Cities {...props}/>
        </Panel>
    );
}

function Cities({country, action}: CitiesOfNoCountryProps) {
    const {t} = useTranslation();
    const query = useCitiesOfNoCountry();
    const skeleton = useSkeleton(query);

    if (skeleton || query.isPending) {
        return (
            <CitiesTable>
                {SKELETON_ROWS.map((row) => (
                    <TableRow key={row}>
                        <TableCell><Skeleton className={styles.bone}/></TableCell>
                        <TableCell><Skeleton className={styles.bone}/></TableCell>
                        <TableCell><Skeleton className={styles.bone}/></TableCell>
                        <TableCell><Skeleton className={styles.bone}/></TableCell>
                    </TableRow>
                ))}
            </CitiesTable>
        );
    }
    if (query.isLoadingError) {
        // The error dialog shows why it failed (docs/error-handling.md).
        return null;
    }
    if (query.data.length === 0) {
        return <Text tone="muted">{t('geography.everyCityInCountry')}</Text>;
    }
    return (
        <CitiesTable>
            {query.data.map((city) => (
                <TableRow key={city.id}>
                    <TableCell><TextLink href={cityEditPath(city.id)}>{city.name}</TextLink></TableCell>
                    <TableCell align="end">{city.places}</TableCell>
                    <TableCell>{country(city)}</TableCell>
                    <TableCell align="end">{action(city)}</TableCell>
                </TableRow>
            ))}
        </CitiesTable>
    );
}

/** The columns of the cities, the same for the data and its skeleton. */
function CitiesTable({children}: {readonly children: ReactNode}) {
    const {t} = useTranslation();
    return (
        <Table label={t('geography.citiesOfNoCountry')} fixed>
            <TableHead>
                <TableRow>
                    <TableHeaderCell className={styles.city}>{t('geography.city')}</TableHeaderCell>
                    <TableHeaderCell className={styles.places} align="end">{t('geography.placesColumn')}</TableHeaderCell>
                    <TableHeaderCell>{t('geography.country')}</TableHeaderCell>
                    <TableHeaderCell className={styles.action}/>
                </TableRow>
            </TableHead>
            <TableBody>{children}</TableBody>
        </Table>
    );
}
