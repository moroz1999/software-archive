import {describe, expect, it} from 'vitest';
import {render, screen} from '@testing-library/react';
import {createRequiredContext} from './context';

const [GreetingProvider, useGreeting] = createRequiredContext<string>('Greeting');

function Greeting() {
    return <p>{useGreeting()}</p>;
}

describe(createRequiredContext, () => {
    it('hands the provided value to the components inside', () => {
        render(<GreetingProvider value="Hello"><Greeting/></GreetingProvider>);

        expect(screen.getByText('Hello')).toBeInTheDocument();
    });

    it('fails loudly outside its provider', () => {
        expect(() => render(<Greeting/>)).toThrow('Greeting is used outside its provider');
    });
});
