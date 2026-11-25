import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {OriginList} from './OriginList';

describe(OriginList, () => {
    it('shows every import the entry came from as its archive and the id there, leading to the page there when it has one', () => {
        render(<OriginList origins={[
            {origin: 'caglrc.cc', externalId: '42', url: null},
            {origin: 'vektor-06c.ru', externalId: '7', url: 'https://vektor-06c.ru/elite/'},
        ]}/>);

        expect(screen.getAllByRole('listitem').map((item) => item.textContent)).toStrictEqual(['caglrc.cc42', 'vektor-06c.ru7']);
        expect(screen.getByRole('link', {name: '7'})).toHaveAttribute('href', 'https://vektor-06c.ru/elite/');
        expect(screen.queryByRole('link', {name: '42'})).not.toBeInTheDocument();
    });

    it('says None for an entry no import brought', () => {
        render(<OriginList origins={[]}/>);

        expect(screen.getByText('None')).toBeInTheDocument();
    });
});
