import {useId} from 'react';
import {useTranslation} from 'react-i18next';
import {parseUiLanguage, storeUiLanguage} from '@/shared/lib/i18n';
import {Select} from '@/shared/ui/select';

const OPTIONS = [{value: 'en', label: 'English'}, {value: 'ru', label: 'Русский'}] as const;

/** Changes the language of every user-facing UI text and remembers an explicit choice. */
export function LanguageSwitch() {
    const id = useId();
    const {t, i18n} = useTranslation();
    const language = parseUiLanguage(i18n.resolvedLanguage) ?? 'en';

    return (
        <Select
            id={id}
            value={language}
            onChange={(value) => {
                const next = parseUiLanguage(value);
                if (next !== null) {
                    storeUiLanguage(next);
                    void i18n.changeLanguage(next);
                }
            }}
            options={OPTIONS}
            placeholder={t('language.choose')}
            ariaLabel={t('language.label')}
        />
    );
}
