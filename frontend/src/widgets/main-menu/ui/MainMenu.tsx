import {AUTHORS_PATH} from '@/entities/authors';
import {CATEGORIES_PATH} from '@/entities/categories';
import {PlatformSwitch} from '@/entities/platforms';
import {GROUPS_PATH} from '@/entities/groups';
import {SOFTWARE_LIST_PATH} from '@/entities/software';
import {LanguageSwitch} from '@/features/language-switch';
import {HOME_PATH} from '@/shared/config';
import {Inline} from '@/shared/ui/inline';
import {NavBar, NavBarLink} from '@/shared/ui/nav-bar';
import {useTranslation} from 'react-i18next';

/** The main menu of the archive, over every page: its sections, the current one marked, and the platform switch. */
export function MainMenu() {
    const {t} = useTranslation();
    return (
        <NavBar label={t('navigation.mainMenu')} actions={<Inline gap="sm"><PlatformSwitch/><LanguageSwitch/></Inline>}>
            <NavBarLink href={HOME_PATH} end>{t('navigation.home')}</NavBarLink>
            <NavBarLink href={SOFTWARE_LIST_PATH}>{t('navigation.software')}</NavBarLink>
            <NavBarLink href={AUTHORS_PATH}>{t('navigation.authors')}</NavBarLink>
            <NavBarLink href={GROUPS_PATH}>{t('navigation.groups')}</NavBarLink>
            <NavBarLink href={CATEGORIES_PATH}>{t('navigation.categories')}</NavBarLink>
        </NavBar>
    );
}
