import { type ReactNode } from 'react';
import Typography from '@mui/material/Typography';
import { Badge, Card, EmailLink } from '@/components/ui';
import type { UserResponseDto } from '@/types';
export interface UserRowProps {
    user: UserResponseDto;
    actions?: ReactNode;
}
export const UserRow = ({ user, actions }: UserRowProps) => (<Card>
    <div className="flex items-start justify-between gap-3">
      <div className="flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 600 }}>
            {user.fullName}
          </Typography>
          <Typography variant="caption" color="text.secondary">
            @{user.username}
          </Typography>
        </div>
        <EmailLink
          email={user.email}
          className="mt-1 block text-xs font-normal text-paragraph"
        />
        <div className="mt-2 flex flex-wrap gap-1.5">
          {user.roles.map((role) => (<Badge key={role} tone="info">
              {role}
            </Badge>))}
        </div>
      </div>
      {actions ? <div className="flex flex-wrap gap-2">{actions}</div> : null}
    </div>
  </Card>);
