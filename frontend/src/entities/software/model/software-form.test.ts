import {describe, expect, it} from 'vitest';
import {toReleaseDate, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from './software';
import {toSoftwareFormValues, validateSoftwareForm} from './software-form';

describe(validateSoftwareForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateSoftwareForm({title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector', hardware: [], categories: []})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector', hardware: [], categories: []},
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '  ', releaseDate: '', platform: 'vector', hardware: [], categories: []})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: null, releaseDate: null, platform: 'vector', hardware: [], categories: []},
        });
    });

    it('requires a title', () => {
        expect(validateSoftwareForm({title: '   ', description: '', releaseDate: '', platform: 'vector', hardware: [], categories: []})).toStrictEqual({ok: false, errors: {title: 'required'}});
    });

    it('limits the title to 255 characters', () => {
        expect(validateSoftwareForm({title: 'a'.repeat(256), description: '', releaseDate: '', platform: 'vector', hardware: [], categories: []})).toStrictEqual({ok: false, errors: {title: 'too-long'}});
    });

    it('requires a platform', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: '', hardware: [], categories: []})).toStrictEqual({ok: false, errors: {platform: 'required'}});
    });

    it('names every problem at once', () => {
        expect(validateSoftwareForm({title: '', description: '', releaseDate: '1985', platform: '', hardware: [], categories: []})).toStrictEqual({
            ok: false,
            errors: {title: 'required', releaseDate: 'invalid', platform: 'required'},
        });
    });
});

describe('the hardware of the draft', () => {
    it('is the checked hardware', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: 'vector', hardware: ['ay8910'], categories: []})).toMatchObject({
            ok: true,
            draft: {hardware: ['ay8910'], categories: []},
        });
    });
});

describe(toSoftwareFormValues, () => {
    it('is empty for new software', () => {
        expect(toSoftwareFormValues()).toStrictEqual({title: '', description: '', releaseDate: '', platform: '', hardware: [], categories: []});
    });

    it('holds the values of existing software, unknown ones empty', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: toReleaseDate('1985-09-20'),
            platform: {code: toSoftwarePlatformCode('bk'), name: 'BK'},
            hardware: [],
            categories: [],
        })).toStrictEqual({title: 'Elite', description: '', releaseDate: '1985-09-20', platform: 'bk', hardware: [], categories: []});
    });

    it('holds the codes of the hardware of the software', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: null,
            platform: {code: toSoftwarePlatformCode('vector'), name: 'Vector'},
            hardware: [{code: 'vector06c', name: 'Vector-06C', group: 'computers'}, {code: 'ay8910', name: 'AY-3-8910', group: 'sound'}],
            categories: [],
        }).hardware).toStrictEqual(['vector06c', 'ay8910']);
    });

    it('holds the ids of the categories of the software', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: null,
            platform: {code: toSoftwarePlatformCode('vector'), name: 'Vector'},
            hardware: [],
            categories: [
                {id: '00000000-0000-0000-0006-000000000001', title: 'Games', branch: [{id: '00000000-0000-0000-0006-000000000001', title: 'Games'}]},
                {id: '00000000-0000-0000-0006-000000000002', title: 'Action', branch: [{id: '00000000-0000-0000-0006-000000000002', title: 'Action'}]},
            ],
        }).categories).toStrictEqual(['00000000-0000-0000-0006-000000000001', '00000000-0000-0000-0006-000000000002']);
    });
});

describe('the categories of the draft', () => {
    it('are the checked categories', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: 'vector', hardware: [], categories: ['00000000-0000-0000-0006-000000000001']})).toMatchObject({
            ok: true,
            draft: {categories: ['00000000-0000-0000-0006-000000000001']},
        });
    });
});
