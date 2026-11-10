import {describe, expect, it} from 'vitest';
import {toReleaseId} from './release';
import {toReleaseFormValues, validateReleaseForm} from './release-form';
import {toReleaseDate} from './software';

const EMPTY = toReleaseFormValues();
const AY = {code: 'ay8910', name: 'AY-3-8910', group: 'sound' as const};

describe(validateReleaseForm, () => {
    it('turns the filled form into a draft', () => {
        expect(validateReleaseForm({
            title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: 'With AY music', hardware: ['ay8910'],
        })).toStrictEqual({
            ok: true,
            draft: {title: 'Elite AY', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: 'With AY music', hardware: ['ay8910']},
        });
    });

    it('leaves empty fields unknown: a release without a title is called by its software', () => {
        expect(validateReleaseForm({...EMPTY, title: '  ', version: ' ', description: ' '})).toStrictEqual({
            ok: true,
            draft: {title: null, version: null, type: 'unknown', releaseDate: null, description: null, hardware: []},
        });
    });

    it('refuses a too long title or version and an incomplete date', () => {
        expect(validateReleaseForm({...EMPTY, title: 'a'.repeat(256), version: '1'.repeat(51), releaseDate: '1990-03'})).toStrictEqual({
            ok: false,
            errors: {title: 'too-long', version: 'too-long', releaseDate: 'invalid'},
        });
    });

    it('takes a version of fifty characters', () => {
        expect(validateReleaseForm({...EMPTY, version: '1'.repeat(50)})).toMatchObject({ok: true});
    });

    it('requires a type', () => {
        expect(validateReleaseForm({...EMPTY, type: ''})).toStrictEqual({ok: false, errors: {type: 'required'}});
    });
});

describe(toReleaseFormValues, () => {
    it('starts a new release of unknown type without hardware', () => {
        expect(toReleaseFormValues()).toStrictEqual({title: '', version: '', type: 'unknown', releaseDate: '', description: '', hardware: []});
    });

    it('fills the form with what a release states, its hardware by code', () => {
        expect(toReleaseFormValues({
            id: toReleaseId('00000000-0000-0000-0007-000000000002'),
            title: null,
            version: '1.1',
            type: 'mod',
            releaseDate: toReleaseDate('1990-03-01'),
            description: null,
            hardware: [AY],
        })).toStrictEqual({title: '', version: '1.1', type: 'mod', releaseDate: '1990-03-01', description: '', hardware: ['ay8910']});
    });
});
