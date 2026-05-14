import { Link } from 'react-router-dom';
import type { UserId } from '@/types';
import { PATHS } from '@/pages/paths';
import { isViewerSubject } from '@/utils/isViewerSubject';
const defaultClassName = 'text-primary underline-offset-2 hover:underline';
export interface SelfOrProfileLinkProps {
    subjectUserId: UserId | undefined;
    viewerUserId: UserId | undefined;
    nameLabel: string;
    className?: string;
}
export function SelfOrProfileLink({ subjectUserId, viewerUserId, nameLabel, className = defaultClassName, }: SelfOrProfileLinkProps) {
    if (subjectUserId === undefined)
        return <>—</>;
    if (isViewerSubject(viewerUserId, subjectUserId)) {
        return (<Link to={PATHS.profile} className={className}>
        Вы
      </Link>);
    }
    return (<Link to={PATHS.userDetail(subjectUserId)} className={className}>
      {nameLabel}
    </Link>);
}
