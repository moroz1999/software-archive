import {describe, expect, it} from 'vitest';
import {toGroupId} from './group';
import {type GroupFormValues, toGroupFormValues, validateGroupForm} from './group-form';

const LABEL = '00000000-0000-0000-0004-000000000001';
const FINLAND = '00000000-0000-0000-0002-000000000001';
const HELSINKI = '00000000-0000-0000-0002-000000000011';

const EMPTY: GroupFormValues = {name: '', notes: '', types: [], labels: [], locations: []};
const CREW: GroupFormValues = {...EMPTY, name: 'Crew Ltd', types: ['demogroup']};

describe(validateGroupForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateGroupForm({
            name: 'Crew Ltd',
            notes: 'Since 1990',
            types: ['demogroup', 'label'],
            labels: [{id: LABEL, name: 'Crew', display: true}, {id: null, name: 'CRW', display: false}],
            locations: [{country: FINLAND, city: HELSINKI, primary: true}],
        })).toStrictEqual({
            ok: true,
            draft: {
                name: 'Crew Ltd',
                notes: 'Since 1990',
                types: ['demogroup', 'label'],
                labels: [{id: LABEL, name: 'Crew', display: true}, {id: null, name: 'CRW', display: false}],
                locations: [{country: FINLAND, city: HELSINKI, primary: true}],
            },
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateGroupForm({...CREW, notes: '  ', locations: [{country: FINLAND, city: '', primary: true}]})).toStrictEqual({
            ok: true,
            draft: {name: 'Crew Ltd', notes: null, types: ['demogroup'], labels: [], locations: [{country: FINLAND, city: null, primary: true}]},
        });
    });

    it('needs a name of at most 255 characters', () => {
        expect(validateGroupForm({...CREW, name: ' '})).toStrictEqual({ok: false, errors: {name: 'required'}});
        expect(validateGroupForm({...CREW, name: 'a'.repeat(256)})).toStrictEqual({ok: false, errors: {name: 'too-long'}});
    });

    it('needs a type', () => {
        expect(validateGroupForm({...CREW, types: []})).toStrictEqual({ok: false, errors: {types: 'required'}});
    });

    it('names the problem of every label in its row', () => {
        expect(validateGroupForm({
            ...CREW,
            labels: [{id: null, name: 'Crew', display: true}, {id: null, name: ' ', display: false}],
        })).toStrictEqual({ok: false, errors: {labels: [null, 'required']}});
    });

    it('needs the country of every location', () => {
        expect(validateGroupForm({
            ...CREW,
            locations: [{country: FINLAND, city: '', primary: true}, {country: '', city: '', primary: false}],
        })).toStrictEqual({ok: false, errors: {locations: [null, 'country-required']}});
    });

    it('names every problem at once', () => {
        expect(validateGroupForm(EMPTY)).toStrictEqual({ok: false, errors: {name: 'required', types: 'required'}});
    });
});

describe(toGroupFormValues, () => {
    it('is empty for a new group', () => {
        expect(toGroupFormValues()).toStrictEqual(EMPTY);
    });

    it('holds the values of an existing group, unknown ones empty', () => {
        expect(toGroupFormValues({
            id: toGroupId('00000000-0000-0000-0005-000000000001'),
            name: 'Crew',
            groupName: 'Crew Ltd',
            notes: null,
            types: ['demogroup'],
            labels: [{id: LABEL, name: 'Crew', display: true}],
            locations: [{countryId: FINLAND, country: 'Finland', cityId: null, city: null, primary: true}],
            members: [],
            memberGroups: [],
            groups: [],
        })).toStrictEqual({
            name: 'Crew Ltd',
            notes: '',
            types: ['demogroup'],
            labels: [{id: LABEL, name: 'Crew', display: true}],
            locations: [{country: FINLAND, city: '', primary: true}],
        });
    });
});
