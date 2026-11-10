export type UiLanguage = 'en' | 'ru';

export const SUPPORTED_UI_LANGUAGES: readonly UiLanguage[] = ['en', 'ru'];

const STORAGE_KEY = 'archive.language';

/** A supported UI language from a language tag such as `ru-RU`. */
export function parseUiLanguage(value: string | null | undefined): UiLanguage | null {
    const language = value?.split('-')[0]?.toLowerCase();
    return language === 'en' || language === 'ru' ? language : null;
}

/** The remembered UI language, then the browser language, with English as the fallback. */
export function detectUiLanguage(): UiLanguage {
    try {
        const stored = parseUiLanguage(localStorage.getItem(STORAGE_KEY));
        if (stored !== null) {
            return stored;
        }
    } catch {
        // A browser may refuse its storage; language detection still works without persistence.
    }
    return parseUiLanguage(navigator.language) ?? 'en';
}

/** Remembers an explicit UI language choice when browser storage is available. */
export function storeUiLanguage(language: UiLanguage): void {
    try {
        localStorage.setItem(STORAGE_KEY, language);
    } catch {
        // The choice still applies until the page is left.
    }
}
