import {describe, expect, it} from 'vitest';
import {emailProblem, passwordProblem, userNameProblem} from './account-rules';

describe(userNameProblem, () => {
    it('needs a name of at most 64 characters', () => {
        expect(userNameProblem('  ')).toBe('required');
        expect(userNameProblem('a'.repeat(65))).toBe('too-long');
        expect(userNameProblem('a'.repeat(64))).toBeNull();
    });
});

describe(emailProblem, () => {
    it('needs an address and a domain', () => {
        expect(emailProblem(' ')).toBe('required');
        expect(emailProblem('zed')).toBe('invalid');
        expect(emailProblem('zed@')).toBe('invalid');
        expect(emailProblem('zed @example.com')).toBe('invalid');
        expect(emailProblem(' Zed@Example.com ')).toBeNull();
    });

    it('needs an email of at most 254 characters', () => {
        expect(emailProblem(`${'a'.repeat(243)}@example.com`)).toBe('too-long');
    });
});

describe(passwordProblem, () => {
    it('needs 8 to 128 characters', () => {
        expect(passwordProblem('1234567')).toBe('too-short');
        expect(passwordProblem('12345678')).toBeNull();
        expect(passwordProblem('a'.repeat(129))).toBe('too-long');
    });
});
