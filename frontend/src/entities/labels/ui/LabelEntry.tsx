import {Inline} from '@/shared/ui/inline';
import {Text, TextLink} from '@/shared/ui/typography';

export interface LabelEntryProps {
    readonly name: string;
    readonly href: string;
    /** Shown muted next to the name: whose it is. */
    readonly note: string | null;
}

/** An entry of a catalog of names: the name leading to its owner, with whose it is next to it. */
export function LabelEntry({name, href, note}: LabelEntryProps) {
    return (
        <Inline gap="sm" align="baseline" wrap>
            <Text><TextLink href={href}>{name}</TextLink></Text>
            {note !== null && (
                <>
                    {' '}
                    <Text variant="bodySm" tone="muted">{note}</Text>
                </>
            )}
        </Inline>
    );
}
