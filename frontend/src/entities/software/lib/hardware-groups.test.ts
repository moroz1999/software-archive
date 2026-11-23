import {describe, expect, it} from 'vitest';
import type {Hardware} from '../model/software';
import {groupHardware} from './hardware-groups';

const VECTOR06C: Hardware = {code: 'vector06c', name: 'Vector-06C', group: 'computers', groupName: 'Computers'};
const TAPE: Hardware = {code: 'tape', name: 'Tape', group: 'storage', groupName: 'Storage'};
const AY: Hardware = {code: 'ay8910', name: 'AY-3-8910', group: 'sound', groupName: 'Sound'};
const BEEPER: Hardware = {code: 'beeper', name: 'Beeper', group: 'sound', groupName: 'Sound'};
const GPU: Hardware = {code: 'gpu', name: 'GPU', group: 'video', groupName: 'Video'};

describe(groupHardware, () => {
    it('puts the hardware under its groups in the order the archive gives them, keeping the order of the items', () => {
        expect(groupHardware([VECTOR06C, TAPE, AY, BEEPER, GPU])).toStrictEqual([
            {group: 'computers', label: 'Computers', items: [VECTOR06C]},
            {group: 'storage', label: 'Storage', items: [TAPE]},
            {group: 'sound', label: 'Sound', items: [AY, BEEPER]},
            {group: 'video', label: 'Video', items: [GPU]},
        ]);
    });

    it('names every group by the name the archive gives it', () => {
        expect(groupHardware([{...GPU, groupName: 'Видео'}]).map((section) => section.label)).toStrictEqual(['Видео']);
    });

    it('has no groups without hardware', () => {
        expect(groupHardware([])).toStrictEqual([]);
    });
});
