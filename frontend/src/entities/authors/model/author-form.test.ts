import {describe, expect, it} from 'vitest';
import {toAuthorId} from './author';
import {type AuthorFormValues, toAuthorFormValues, validateAuthorForm} from './author-form';

const LABEL = '00000000-0000-0000-0004-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';
const HELSINKI = '00000000-0000-0000-0002-000000000011';

const EMPTY: AuthorFormValues = {realName: '', notes: '', labels: [], locations: []};

describe(validateAuthorForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateAuthorForm({
            realName: 'John Doe',
            notes: 'Coder since 1984',
            labels: [{id: LABEL, name: 'Zed', display: true}, {id: null, name: 'Axe', display: false}],
            locations: [{country: FINLAND, city: HELSINKI, primary: true}],
        })).toStrictEqual({
            ok: true,
            draft: {
                realName: 'John Doe',
                notes: 'Coder since 1984',
                labels: [{id: LABEL, name: 'Zed', display: true}, {id: null, name: 'Axe', display: false}],
                locations: [{country: FINLAND, city: HELSINKI, primary: true}],
            },
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateAuthorForm({...EMPTY, realName: 'John Doe', notes: '  ', locations: [{country: FINLAND, city: '', primary: false}]})).toStrictEqual({
            ok: true,
            draft: {realName: 'John Doe', notes: null, labels: [], locations: [{country: FINLAND, city: null, primary: false}]},
        });
    });

    it('needs a real name or a label', () => {
        expect(validateAuthorForm(EMPTY)).toStrictEqual({ok: false, errors: {realName: 'nameless'}});
        expect(validateAuthorForm({...EMPTY, realName: '  '})).toStrictEqual({ok: false, errors: {realName: 'nameless'}});
        expect(validateAuthorForm({...EMPTY, labels: [{id: null, name: 'Zed', display: false}]})).toMatchObject({ok: true});
    });

    it('limits the real name to 255 characters', () => {
        expect(validateAuthorForm({...EMPTY, realName: 'a'.repeat(256)})).toStrictEqual({ok: false, errors: {realName: 'too-long'}});
    });

    it('names the problem of every label in its row', () => {
        expect(validateAuthorForm({
            ...EMPTY,
            labels: [{id: null, name: 'Zed', display: false}, {id: null, name: ' ', display: false}, {id: null, name: 'a'.repeat(256), display: false}],
        })).toStrictEqual({ok: false, errors: {labels: [null, 'required', 'too-long']}});
    });

    it('needs the country of every location', () => {
        expect(validateAuthorForm({
            ...EMPTY,
            realName: 'John Doe',
            locations: [{country: FINLAND, city: '', primary: false}, {country: '', city: '', primary: false}],
        })).toStrictEqual({ok: false, errors: {locations: [null, 'country-required']}});
    });
});

describe(toAuthorFormValues, () => {
    it('is empty for a new author', () => {
        expect(toAuthorFormValues()).toStrictEqual(EMPTY);
    });

    it('holds the values of an existing author, unknown ones empty', () => {
        expect(toAuthorFormValues({
            id: toAuthorId('00000000-0000-0000-0003-000000000001'),
            name: 'Zed',
            realName: null,
            notes: null,
            labels: [{id: LABEL, name: 'Zed', display: true}],
            locations: [{countryId: FINLAND, country: 'Finland', cityId: null, city: null, primary: true}],
            groups: [],
        })).toStrictEqual({
            realName: '',
            notes: '',
            labels: [{id: LABEL, name: 'Zed', display: true}],
            locations: [{country: FINLAND, city: '', primary: true}],
        });
    });
});
