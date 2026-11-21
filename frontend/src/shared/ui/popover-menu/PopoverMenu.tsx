import {type ReactNode, useCallback, useMemo, useState} from 'react';
import {useLocation} from 'react-router';
import {Button} from '../button';
import {PencilIcon} from '../icon';
import {IconPopover} from '../icon-popover';
import {Stack} from '../stack';
import {PopoverMenuContext, usePopoverMenu} from './popover-menu-context';

export interface PopoverMenuProps {
    /** Names the pencil button and the window it opens. */
    readonly label: string;
    /** The items, `PopoverMenuItem`. */
    readonly children: ReactNode;
}

/** A pencil button opening a column of the actions of a page from its left edge; choosing one closes it. */
export function PopoverMenu({label, children}: PopoverMenuProps) {
    const location = useLocation();
    // It remembers where it was opened: a link followed from it leads elsewhere, where it is closed.
    const [openAt, setOpenAt] = useState<string | null>(null);
    if (openAt !== null && openAt !== location.key) {
        setOpenAt(null);
    }
    const onOpenChange = useCallback((open: boolean) => {
        setOpenAt(open ? location.key : null);
    }, [location.key]);
    const menu = useMemo(() => ({close: () => {
        setOpenAt(null);
    }}), []);

    return (
        <IconPopover label={label} icon={<PencilIcon/>} open={openAt !== null} onOpenChange={onOpenChange} align="start">
            <PopoverMenuContext value={menu}>
                <Stack gap="xs">{children}</Stack>
            </PopoverMenuContext>
        </IconPopover>
    );
}

export type PopoverMenuItemProps = {readonly children: ReactNode} & ({readonly href: string} | {readonly onClick: () => void});

/** An action of a popover menu: a link, or a button that closes the menu before it runs. */
export function PopoverMenuItem(props: PopoverMenuItemProps) {
    const {close} = usePopoverMenu();
    if ('href' in props) {
        return <Button href={props.href} color="secondary" size="sm" block>{props.children}</Button>;
    }
    return (
        <Button color="secondary" size="sm" block onClick={() => {
            close();
            props.onClick();
        }}>
            {props.children}
        </Button>
    );
}
