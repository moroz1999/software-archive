import {describe, expect, it} from 'vitest';
import {toSoftwareFormValues, validateSoftwareForm} from './software-form';

describe(validateSoftwareForm, () => {
    it('turns filled values into a draft', () => {
        expect(validateSoftwareForm({title: 'Elite', description: 'Space trading', releaseDate: '20.09.1985', platform: 'vector', hardware: [], categories: []})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: 'Space trading', releaseDate: '1985-09-20', platform: 'vector', hardware: [], categories: [], credits: [], preservation: 'unknown', instructions: null, lineage: {kind: null, reliability: null, rework: [], source: null}},
        });
    });

    it('turns empty optional values into unknown ones', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '  ', releaseDate: '', platform: 'vector', hardware: [], categories: []})).toStrictEqual({
            ok: true,
            draft: {title: 'Elite', description: null, releaseDate: null, platform: 'vector', hardware: [], categories: [], credits: [], preservation: 'unknown', instructions: null, lineage: {kind: null, reliability: null, rework: [], source: null}},
        });
    });

    it('takes the chosen preservation status', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: 'vector', hardware: [], categories: [], preservation: 'missing-in-action'})).toMatchObject({
            ok: true,
            draft: {preservation: 'missing-in-action'},
        });
    });

    it('takes a year alone as the release date', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '1985', platform: 'vector', hardware: [], categories: []})).toMatchObject({
            ok: true,
            draft: {releaseDate: '1985'},
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

    it('takes a day only as dd.mm.yyyy, and only a day of the calendar', () => {
        for (const releaseDate of ['1985-09-20', '31.02.1985', '1.9.1985']) {
            expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate, platform: 'vector', hardware: [], categories: []})).toStrictEqual({
                ok: false,
                errors: {releaseDate: 'invalid'},
            });
        }
    });

    it('names every problem at once', () => {
        expect(validateSoftwareForm({title: '', description: '', releaseDate: '1985-09', platform: '', hardware: [], categories: []})).toStrictEqual({
            ok: false,
            errors: {title: 'required', releaseDate: 'invalid', platform: 'required'},
        });
    });
});

describe('the instructions of the draft', () => {
    it('are the text given, blank ones none', () => {
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: 'vector', hardware: [], categories: [], instructions: 'Press Space'})).toMatchObject({
            ok: true,
            draft: {instructions: 'Press Space'},
        });
        expect(validateSoftwareForm({title: 'Elite', description: '', releaseDate: '', platform: 'vector', hardware: [], categories: [], instructions: '  '})).toMatchObject({
            ok: true,
            draft: {instructions: null},
        });
    });
});

describe('the lineage of the draft', () => {
    const base = {title: 'Elite', description: '', releaseDate: '', platform: 'bk', hardware: [], categories: []};

    it('is the kind, the reliability, the rework and the original chosen', () => {
        expect(validateSoftwareForm({
            ...base,
            lineage: {kind: 'port-with-changes', reliability: 'reliable', rework: ['new-levels-added', 'graphics-and-sound-changed'], source: 'Elite, BBC Micro'},
        })).toMatchObject({
            ok: true,
            draft: {lineage: {kind: 'port-with-changes', reliability: 'reliable', rework: ['new-levels-added', 'graphics-and-sound-changed'], source: 'Elite, BBC Micro'}},
        });
    });

    it('is unknown in every part left empty, a blank original included', () => {
        expect(validateSoftwareForm({...base, lineage: {kind: '', reliability: '', rework: [], source: '   '}})).toMatchObject({
            ok: true,
            draft: {lineage: {kind: null, reliability: null, rework: [], source: null}},
        });
    });

    it('never tells a reliability or a rework without a kind', () => {
        expect(validateSoftwareForm({...base, lineage: {kind: '', reliability: 'reliable', rework: ['new-implementation'], source: 'Elite'}})).toMatchObject({
            ok: true,
            draft: {lineage: {kind: null, reliability: null, rework: [], source: 'Elite'}},
        });
    });

    it('limits the original to 255 characters', () => {
        expect(validateSoftwareForm({...base, lineage: {kind: '', reliability: '', rework: [], source: 'a'.repeat(256)}})).toStrictEqual({ok: false, errors: {lineageSource: 'too-long'}});
        expect(validateSoftwareForm({...base, lineage: {kind: '', reliability: '', rework: [], source: 'a'.repeat(255)}})).toMatchObject({ok: true});
    });

    it('is held by the form values of existing software', () => {
        expect(toSoftwareFormValues({
            origins: [],
            compilationItems: [], compilations: [], credits: [], sources: [], links: [], instructions: null,
            added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true, preservation: 'unknown',
            lineage: {kind: 'remake', reliability: null, rework: ['new-implementation'], source: 'Elite, BBC Micro'},
            id: '00000000-0000-0000-0001-000000000001',
            title: 'Elite',
            description: null,
            releaseDate: null,
            platform: {code: 'vector', name: 'Vector'},
            hardware: [],
            categories: [],
        }).lineage).toStrictEqual({kind: 'remake', reliability: '', rework: ['new-implementation'], source: 'Elite, BBC Micro'});
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
        expect(toSoftwareFormValues()).toStrictEqual({title: '', description: '', releaseDate: '', platform: '', hardware: [], categories: [], credits: [], preservation: 'unknown', instructions: '', lineage: {kind: '', reliability: '', rework: [], source: ''}});
    });

    it('holds the values of existing software, unknown ones empty', () => {
        expect(toSoftwareFormValues({
            origins: [],
            compilationItems: [], compilations: [], credits: [], sources: [], links: [], instructions: 'Press Space to start',
            added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true, lineage: {kind: null, reliability: null, rework: [], source: null},
            id: '00000000-0000-0000-0001-000000000001',
            title: 'Elite',
            description: null,
            releaseDate: '1985-09-20',
            platform: {code: 'bk', name: 'BK'},
            hardware: [],
            categories: [],
            preservation: 'in-sales',
        })).toStrictEqual({title: 'Elite', description: '', releaseDate: '20.09.1985', platform: 'bk', hardware: [], categories: [], credits: [], compilationItems: [], preservation: 'in-sales', instructions: 'Press Space to start', lineage: {kind: '', reliability: '', rework: [], source: ''}});
    });

    it('holds the codes of the hardware of the software', () => {
        expect(toSoftwareFormValues({
            origins: [],
            compilationItems: [], compilations: [], credits: [], sources: [], links: [], instructions: null,
            added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true, preservation: 'unknown', lineage: {kind: null, reliability: null, rework: [], source: null},
            id: '00000000-0000-0000-0001-000000000001',
            title: 'Elite',
            description: null,
            releaseDate: null,
            platform: {code: 'vector', name: 'Vector'},
            hardware: [{code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'}, {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'}],
            categories: [],
        }).hardware).toStrictEqual(['vector06c', 'ay8910']);
    });

    it('holds the ids of the categories of the software', () => {
        expect(toSoftwareFormValues({
            origins: [],
            compilationItems: [], compilations: [], credits: [], sources: [], links: [], instructions: null,
            added: {by: 'Zed', at: '2026-01-01T00:00:00Z'}, canChange: true, preservation: 'unknown', lineage: {kind: null, reliability: null, rework: [], source: null},
            id: '00000000-0000-0000-0001-000000000001',
            title: 'Elite',
            description: null,
            releaseDate: null,
            platform: {code: 'vector', name: 'Vector'},
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
