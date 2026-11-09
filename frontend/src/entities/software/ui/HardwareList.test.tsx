import {describe, expect, it} from 'vitest';
import {render, screen, within} from '@testing-library/react';
import {HardwareList} from './HardwareList';

describe(HardwareList, () => {
    it('shows every group by name over the names of its hardware', () => {
        render(<HardwareList hardware={[
            {code: 'vector06c', name: 'Vector-06C', group: 'computers'},
            {code: 'ay8910', name: 'AY-3-8910', group: 'sound'},
            {code: 'beeper', name: 'Beeper', group: 'sound'},
        ]}/>);

        const sound = screen.getByRole('group', {name: 'Sound'});

        expect(screen.getAllByRole('group').map((group) => group.getAttribute('aria-label'))).toStrictEqual(['Computers', 'Sound']);
        expect(within(sound).getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['AY-3-8910', 'Beeper']);
        expect(within(screen.getByRole('group', {name: 'Computers'})).getByText('Vector-06C')).toBeInTheDocument();
    });

    it('shows nothing without hardware', () => {
        const {container} = render(<HardwareList hardware={[]}/>);

        expect(container).toBeEmptyDOMElement();
    });
});
