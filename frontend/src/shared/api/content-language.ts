import {detectUiLanguage, type UiLanguage} from '../lib/i18n';

let chosen: UiLanguage | null = null;

/**
 * Asks every later call for archive content in the given language; null asks in the detected UI language again
 * (docs/features/localization.md).
 */
export function setContentLanguage(language: UiLanguage | null): void {
    chosen = language;
}

/** The language archive content is asked in: the one set, else the detected UI language. */
export function contentLanguage(): UiLanguage {
    return chosen ?? detectUiLanguage();
}
