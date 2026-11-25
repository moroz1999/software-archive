import {useSyncExternalStore} from 'react';

function subscribe(onChange: () => void): () => void {
    const observer = new MutationObserver(onChange);
    observer.observe(document.head, {subtree: true, childList: true, characterData: true});
    return () => {
        observer.disconnect();
    };
}

function title(): string {
    return document.title;
}

/**
 * The title of the browser tab, again whenever it changes: a page names its tab when it renders (`PageLayout`),
 * after the route changed and often once its data came.
 */
export function useDocumentTitle(): string {
    return useSyncExternalStore(subscribe, title);
}
