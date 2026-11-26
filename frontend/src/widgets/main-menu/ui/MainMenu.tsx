import {AUTHORS_PATH} from '@/entities/authors';
import {CATEGORIES_PATH} from '@/entities/categories';
import {PlatformSwitch} from '@/entities/platforms';
import {GROUPS_PATH} from '@/entities/groups';
import {SOFTWARE_LIST_PATH} from '@/entities/software';
import {MANAGEMENT_PATH, useAbility, useCurrentUser} from '@/entities/users';
import {AssistantPanel} from '@/features/assistant-chat';
import {LanguageSwitch} from '@/features/language-switch';
import {QuickSearch} from '@/features/search';
import {AccountMenu} from '@/features/user/sign-in';
import {HOME_PATH, SITE_NAME} from '@/shared/config';
import {Inline} from '@/shared/ui/inline';
import {NavBar, NavBarLink} from '@/shared/ui/nav-bar';
import {useTranslation} from 'react-i18next';
import logoDark from './logo-dark.svg';
import logoLight from './logo-light.svg';

/** The main menu of the archive, over every page: its sections, the current one marked, the quick search, the switches and the account. */
export function MainMenu() {
    const {t} = useTranslation();
    const administrator = useCurrentUser().user?.groups.includes('admin') ?? false;
    const assistant = useAbility('use-assistant');
    return (
        <NavBar
            label={t('navigation.mainMenu')}
            brand={{src: logoLight, darkSrc: logoDark, alt: SITE_NAME, width: 95, height: 36, href: HOME_PATH}}
            actions={<Inline gap="sm" justify="end" wrap>{assistant && <AssistantPanel/>}<QuickSearch/><PlatformSwitch/><LanguageSwitch/><AccountMenu/></Inline>}
        >
            <NavBarLink href={HOME_PATH} end>{t('navigation.home')}</NavBarLink>
            <NavBarLink href={SOFTWARE_LIST_PATH}>{t('navigation.software')}</NavBarLink>
            <NavBarLink href={AUTHORS_PATH}>{t('navigation.authors')}</NavBarLink>
            <NavBarLink href={GROUPS_PATH}>{t('navigation.groups')}</NavBarLink>
            {administrator && <NavBarLink href={CATEGORIES_PATH} activePath={MANAGEMENT_PATH}>{t('navigation.management')}</NavBarLink>}
        </NavBar>
    );
}
