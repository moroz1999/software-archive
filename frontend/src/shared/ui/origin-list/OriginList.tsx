import {useTranslation} from 'react-i18next';
import type {ExternalOrigin} from '@/shared/api';
import {Inline} from '../inline';
import {Stack} from '../stack';
import {Text, TextLink} from '../typography';

export interface OriginListProps {
    readonly origins: readonly ExternalOrigin[];
}

/**
 * Every import an entry came from, as text: the source archive and the id there, the id leading to the page there when
 * it has one; "None" for an entry no import brought.
 */
export function OriginList({origins}: OriginListProps) {
    const {t} = useTranslation();
    if (origins.length === 0) {
        return <Text tone="muted">{t('origins.none')}</Text>;
    }
    return (
        <Stack as="ul" gap="xs">
            {origins.map((origin) => (
                <li key={`${origin.origin} ${origin.externalId}`}>
                    <Inline gap="sm" align="baseline" wrap>
                        <Text tone="muted">{origin.origin}</Text>
                        {origin.url === null ? <Text>{origin.externalId}</Text> : <Text><TextLink href={origin.url}>{origin.externalId}</TextLink></Text>}
                    </Inline>
                </li>
            ))}
        </Stack>
    );
}
