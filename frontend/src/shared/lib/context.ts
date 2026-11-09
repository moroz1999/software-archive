import {createContext, type Provider, useContext} from 'react';

/**
 * A context that must be provided: the hook fails loudly when a component is used outside its provider,
 * instead of working with a made-up default.
 */
export function createRequiredContext<T>(name: string): readonly [Provider<T | null>, () => T] {
    const Context = createContext<T | null>(null);
    Context.displayName = name;
    const use = (): T => {
        const value = useContext(Context);
        if (value === null) {
            throw new Error(`${name} is used outside its provider`);
        }
        return value;
    };
    return [Context.Provider, use] as const;
}
