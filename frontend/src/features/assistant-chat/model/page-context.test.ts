import {describe, expect, it} from 'vitest';
import {pageContextOf} from './page-context';

const ID = '00000000-0000-0000-0001-000000000001';
const RELEASE = '00000000-0000-0000-0002-000000000001';

describe(pageContextOf, () => {
    it.each([
        [`/software/${ID}`, 'software', ID],
        [`/software/${ID}/edit`, 'software', ID],
        [`/software/${ID}/releases/${RELEASE}`, 'release', RELEASE],
        [`/software/${ID}/releases/${RELEASE}/edit`, 'release', RELEASE],
        [`/authors/${ID}`, 'author', ID],
        [`/groups/${ID}/edit`, 'group', ID],
        [`/labels/${ID}`, 'label', ID],
        [`/management/geography/countries/${ID}/edit`, 'country', ID],
        [`/management/geography/cities/${ID}/edit`, 'city', ID],
        ['/management/hardware/zx48/edit', 'hardware', 'zx48'],
    ])('names the entry the page %s shows', (path, entityType, entityId) => {
        expect(pageContextOf(path, 'Title')).toStrictEqual({path, title: 'Title', entityType, entityId});
    });

    it.each([
        '/',
        '/software',
        '/software/new',
        `/software/${ID}/releases/new`,
        '/authors/new',
        '/management/hardware/groups/zx/edit',
        '/software/zed',
    ])('names no entry for the page %s', (path) => {
        expect(pageContextOf(path, 'Title')).toStrictEqual({path, title: 'Title', entityType: null, entityId: null});
    });
});
