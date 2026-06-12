import { describe, expect, it } from 'vitest';
import { getApiErrorMessage } from './errors';

describe('getApiErrorMessage', () => {
  it('uses an Error message when available', () => {
    expect(getApiErrorMessage(new Error('Request failed'), 'Fallback')).toBe('Request failed');
  });

  it('uses the fallback for unknown failures', () => {
    expect(getApiErrorMessage({ unexpected: true }, 'Fallback')).toBe('Fallback');
  });
});
