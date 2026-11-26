import {useTranslation} from 'react-i18next';
import {Chip} from '@/shared/ui/chip';
import {Inline} from '@/shared/ui/inline';
import {Stack} from '@/shared/ui/stack';
import {Text, TextLink} from '@/shared/ui/typography';
import type {SoftwareCredit} from '../model/software';

/** Where a credited name leads: the page of the person or the group, a label to its own; null when nowhere. */
export type CreditHref = (credit: SoftwareCredit) => string | null;

export interface SoftwareCreditListProps {
    readonly credits: readonly SoftwareCredit[];
    readonly creditHref?: CreditHref | undefined;
    /** Small, without a name of its own: inside a row of releases or on a card. */
    readonly compact?: boolean;
    /** The enclosing field already identifies the role. */
    readonly namesOnly?: boolean;
}

/** Whoever is credited on software or a release, each with the roles known (docs/domain/software.md#credits). */
export function SoftwareCreditList({credits, creditHref, compact = false, namesOnly = false}: SoftwareCreditListProps) {
    const {t} = useTranslation();
    const variant = compact ? 'bodySm' : 'body';
    return (
        <Stack as="ul" gap="xs" {...(compact || namesOnly ? {} : {ariaLabel: t('fields.credits')})}>
            {credits.map((credit) => {
                const href = creditHref?.(credit) ?? null;
                const roles = namesOnly ? [] : credit.roles.filter((role) => role !== 'unknown');
                return (
                    <li key={`${credit.type}:${credit.id}`}>
                        <Inline gap="xs" align="center" wrap>
                            <Text as="span" variant={variant}>{href === null ? credit.title : <TextLink href={href}>{credit.title}</TextLink>}</Text>
                            {roles.map((role) => <Chip key={role} size="sm">{t(`roles.software.${role}`)}</Chip>)}
                        </Inline>
                    </li>
                );
            })}
        </Stack>
    );
}
