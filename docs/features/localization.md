# UI Localization

The interface ships English and Russian translations with the frontend. The language control is in the main menu beside the platform switch.

The explicit choice is kept in browser storage under `archive.language`. Without one, the frontend uses a supported browser language and falls back to English. The document language follows the active choice for assistive technology, and locale-sensitive UI values use the same language.
