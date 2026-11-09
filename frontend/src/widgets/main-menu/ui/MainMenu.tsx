import {AUTHORS_PATH} from '@/entities/authors';
import {SOFTWARE_LIST_PATH} from '@/entities/software';
import {HOME_PATH} from '@/shared/config';
import {NavBar, NavBarLink} from '@/shared/ui/nav-bar';

/** The main menu of the archive, over every page: its sections, the current one marked. */
export function MainMenu() {
    return (
        <NavBar label="Main menu">
            <NavBarLink href={HOME_PATH} end>Home</NavBarLink>
            <NavBarLink href={SOFTWARE_LIST_PATH}>Software</NavBarLink>
            <NavBarLink href={AUTHORS_PATH}>Authors</NavBarLink>
        </NavBar>
    );
}
