import {useEffect} from 'react';
import {useQueryClient} from '@tanstack/react-query';
import {useTranslation} from 'react-i18next';
import {setContentLanguage} from '@/shared/api';
import {parseUiLanguage} from '@/shared/lib/i18n';

/**
 * Asks for the archive content in the UI language and, when the language is switched, loads what is shown again in
 * the new one (docs/features/localization.md).
 */
export function ContentLanguageSync() {
    const {i18n} = useTranslation();
    const queryClient = useQueryClient();

    useEffect(() => {
        setContentLanguage(parseUiLanguage(i18n.language));
        const switchContent = (language: string) => {
            setContentLanguage(parseUiLanguage(language));
            void queryClient.invalidateQueries();
        };
        i18n.on('languageChanged', switchContent);
        return () => {
            i18n.off('languageChanged', switchContent);
            setContentLanguage(null);
        };
    }, [i18n, queryClient]);

    return null;
}
