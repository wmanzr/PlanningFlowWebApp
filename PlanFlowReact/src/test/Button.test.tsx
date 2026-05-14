import { describe, expect, it, vi } from 'vitest';
import { render, screen } from '@testing-library/react';
import userEvent from '@testing-library/user-event';
import { Button } from '@/components/ui/Button';
describe('Button', () => {
    it('renders children', () => {
        render(<Button>Click me</Button>);
        expect(screen.getByRole('button', { name: 'Click me' })).toBeInTheDocument();
    });
    it('invokes onClick', async () => {
        const handler = vi.fn();
        render(<Button onClick={handler}>Go</Button>);
        await userEvent.click(screen.getByRole('button'));
        expect(handler).toHaveBeenCalledTimes(1);
    });
    it('disables when loading', () => {
        render(<Button loading>Saving</Button>);
        expect(screen.getByRole('button')).toBeDisabled();
    });
});
