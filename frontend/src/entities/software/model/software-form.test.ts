import {describe, expect, it} from 'vitest';
import {toReleaseDate, toSoftwareId, toSoftwareTitle} from './software';
import {toSoftwareFormValues, validateSoftwareForm} from './software-form';

describe(validateSoftwareForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateSoftwareForm({title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20'})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20'},
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '  ', releaseDate: ''})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: null, releaseDate: null},
        });
    });

    it('requires a title', () => {
        expect(validateSoftwareForm({title: '   ', description: '', releaseDate: ''})).toStrictEqual({ok: false, errors: {title: 'required'}});
    });

    it('limits the title to 255 characters', () => {
        expect(validateSoftwareForm({title: 'a'.repeat(256), description: '', releaseDate: ''})).toStrictEqual({ok: false, errors: {title: 'too-long'}});
    });
});

describe(toSoftwareFormValues, () => {
    it('is empty for new software', () => {
        expect(toSoftwareFormValues()).toStrictEqual({title: '', description: '', releaseDate: ''});
    });

    it('holds the values of existing software, unknown ones empty', () => {
        expect(toSoftwareFormValues({
            id: toSoftwareId('00000000-0000-0000-0001-000000000001'),
            title: toSoftwareTitle('Elite'),
            description: null,
            releaseDate: toReleaseDate('1985-09-20'),
        })).toStrictEqual({title: 'Elite', description: '', releaseDate: '1985-09-20'});
    });
});
