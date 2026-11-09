import {describe, expect, it} from 'vitest';
import type {Hardware, HardwareGroup} from '../model/software';
import {groupHardware} from './hardware-groups';

const VECTOR06C: Hardware = {code: 'vector06c', name: 'Vector-06C', group: 'computers'};
const TAPE: Hardware = {code: 'tape', name: 'Tape', group: 'storage'};
const AY: Hardware = {code: 'ay8910', name: 'AY-3-8910', group: 'sound'};
const BEEPER: Hardware = {code: 'beeper', name: 'Beeper', group: 'sound'};

describe(groupHardware, () => {
    it('puts the hardware under its groups in their fixed order, keeping the order of the items', () => {
        expect(groupHardware([AY, TAPE, BEEPER, VECTOR06C])).toStrictEqual([
            {group: 'computers', label: 'Computers', items: [VECTOR06C]},
            {group: 'storage', label: 'Storage', items: [TAPE]},
            {group: 'sound', label: 'Sound', items: [AY, BEEPER]},
        ]);
    });

    it('names every group', () => {
        const groups: readonly HardwareGroup[] = ['expansion', 'controls', 'dos'];

        expect(groupHardware(groups.map((group) => ({code: group, name: group, group}))).map((section) => section.label))
            .toStrictEqual(['DOS', 'Controls', 'Expansion']);
    });

    it('has no groups without hardware', () => {
        expect(groupHardware([])).toStrictEqual([]);
    });
});
