import { type AnchorHTMLAttributes } from 'react';
import { cn } from '../cn';

export interface EmailLinkProps extends Omit<AnchorHTMLAttributes<HTMLAnchorElement>, 'href'> {
    email: string;
}

export function EmailLink({ email, className, onClick, ...rest }: EmailLinkProps) {
    const trimmed = email.trim();
    if (!trimmed) {
        return null;
    }
    return (
        <a
            href={`mailto:${trimmed}`}
            className={cn('font-medium text-highlight underline-offset-4 hover:underline', className)}
            onClick={(e) => {
                e.stopPropagation();
                onClick?.(e);
            }}
            {...rest}
        >
            {trimmed}
        </a>
    );
}
