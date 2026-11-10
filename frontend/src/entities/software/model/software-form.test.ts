import {describe, expect, it} from 'vitest';
import {toReleaseDate, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from './software';
import {toSoftwareFormValues, validateSoftwareForm} from './software-form';

describe(validateSoftwareForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateSoftwareForm({title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector', hardware: []})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector', hardware: []},
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '  ', releaseDate: '', platform: 'vector', hardware: []})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: null, releaseDate: null, platform: 'vector', hardware: []},
        });
    });

    it('requires a title', () => {
        expect(validateSoftwareForm({title: '   ', description: '', releaseDate: '', platform: 'vector', hardware: []})).toStrictEqual({ok: false, errors: {title: 'required'}});
    });

    it('limits the title to 255 characters', () => {
        expect(validateSoftwareForm({title: 'a'.repeat(256), description: '', releaseDate: '', platform: 'vector', hardware: []})).toStrictEqual({ok: false, errors: {title: 'too-long'}});
    });

    it('requires a platform', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: '', hardware: []})).toStrictEqual({ok: false, errors: {platform: 'required'}});
    });

    it('names every problem at once', () => {
        expect(validateSoftwareForm({title: '', description: '', releaseDate: '1985', platform: '', hardware: []})).toStrictEqual({
            ok: false,
            errors: {title: 'required', releaseDate: 'invalid', platform: 'required'},
        });
    });
});

describe('the hardware of the draft', () => {
    it('is the checked hardware', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: 'vector', hardware: ['ay8910']})).toMatchObject({
            ok: true,
            draft: {hardware: ['ay8910']},
        });
    });
});

describe(toSoftwareFormValues, () => {
    it('is empty for new software', () => {
        expect(toSoftwareFormValues()).toStrictEqual({title: '', description: '', releaseDate: '', platform: '', hardware: []});
    });

    it('holds the values of existing software, unknown ones empty', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: toReleaseDate('1985-09-20'),
            platform: {code: toSoftwarePlatformCode('bk'), name: 'BK'},
            hardware: [],
        })).toStrictEqual({title: 'Elite', description: '', releaseDate: '1985-09-20', platform: 'bk', hardware: []});
    });

    it('holds the codes of the hardware of the software', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: null,
            platform: {code: toSoftwarePlatformCode('vector'), name: 'Vector'},
            hardware: [{code: 'vector06c', name: 'Vector-06C', group: 'computers'}, {code: 'ay8910', name: 'AY-3-8910', group: 'sound'}],
        }).hardware).toStrictEqual(['vector06c', 'ay8910']);
    });
});
