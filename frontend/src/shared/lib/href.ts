/** An href of a route inside this app ("/software"), navigated client-side; not protocol-relative ("//host"). */
export function isAppHref(href: string): boolean {
    return href.startsWith('/') && !href.startsWith('//');
}
