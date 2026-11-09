import {Outlet} from 'react-router';
import {MainMenu} from '@/widgets/main-menu';

/** The frame of every page: the main menu over the page of the current route. */
export function AppLayout() {
    return (
        <>
            <MainMenu/>
            <Outlet/>
        </>
    );
}
