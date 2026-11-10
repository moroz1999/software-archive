import {type ReactNode, useEffect, useState} from 'react';
import {createInstance} from 'i18next';
import {I18nextProvider as ReactI18nextProvider, initReactI18next} from 'react-i18next';
import {detectUiLanguage, parseUiLanguage, SUPPORTED_UI_LANGUAGES} from '@/shared/lib/i18n';
import {resources} from './resources';

function createI18n() {
    const language = detectUiLanguage();
    const instance = createInstance();
    void instance.use(initReactI18next).init({
        resources,
        lng: language,
        fallbackLng: 'en',
        supportedLngs: SUPPORTED_UI_LANGUAGES,
        initAsync: false,
        interpolation: {escapeValue: false},
        react: {useSuspense: false},
    });
    document.documentElement.lang = language;
    return instance;
}

/** Supplies the bundled UI translations and keeps the document language in sync. */
export function I18nProvider({children}: {readonly children: ReactNode}) {
    const [i18n] = useState(createI18n);

    useEffect(() => {
        const syncDocumentLanguage = (language: string) => {
            document.documentElement.lang = parseUiLanguage(language) ?? 'en';
        };
        i18n.on('languageChanged', syncDocumentLanguage);
        return () => {
            i18n.off('languageChanged', syncDocumentLanguage);
        };
    }, [i18n]);

    return <ReactI18nextProvider i18n={i18n}>{children}</ReactI18nextProvider>;
}
