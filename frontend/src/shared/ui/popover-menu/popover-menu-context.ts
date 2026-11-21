import {createRequiredContext} from '@/shared/lib/context';

/** Closes the menu an item is in. */
export const [PopoverMenuContext, usePopoverMenu] = createRequiredContext<{readonly close: () => void}>('PopoverMenu');
