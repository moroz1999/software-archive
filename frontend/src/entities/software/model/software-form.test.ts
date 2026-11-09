import {describe, expect, it} from 'vitest';
import {toReleaseDate, toSoftwareId, toSoftwarePlatformCode, toSoftwareTitle} from './software';
import {toSoftwareFormValues, validateSoftwareForm} from './software-form';

describe(validateSoftwareForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateSoftwareForm({title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector'})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector'},
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '  ', releaseDate: '', platform: 'vector'})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: null, releaseDate: null, platform: 'vector'},
        });
    });

    it('requires a title', () => {
        expect(validateSoftwareForm({title: '   ', description: '', releaseDate: '', platform: 'vector'})).toStrictEqual({ok: false, errors: {title: 'required'}});
    });

    it('limits the title to 255 characters', () => {
        expect(validateSoftwareForm({title: 'a'.repeat(256), description: '', releaseDate: '', platform: 'vector'})).toStrictEqual({ok: false, errors: {title: 'too-long'}});
    });

    it('requires a platform', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: ''})).toStrictEqual({ok: false, errors: {platform: 'required'}});
    });

    it('names every problem at once', () => {
        expect(validateSoftwareForm({title: '', description: '', releaseDate: '1985', platform: ''})).toStrictEqual({
            ok: false,
            errors: {title: 'required', releaseDate: 'invalid', platform: 'required'},
        });
    });
});

describe(toSoftwareFormValues, () => {
    it('is empty for new software', () => {
        expect(toSoftwareFormValues()).toStrictEqual({title: '', description: '', releaseDate: '', platform: ''});
    });

    it('holds the values of existing software, unknown ones empty', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: toReleaseDate('1985-09-20'),
            platform: {code: toSoftwarePlatformCode('bk'), name: 'BK'},
            hardware: [],
        })).toStrictEqual({title: 'Elite', description: '', releaseDate: '1985-09-20', platform: 'bk'});
    });
});
