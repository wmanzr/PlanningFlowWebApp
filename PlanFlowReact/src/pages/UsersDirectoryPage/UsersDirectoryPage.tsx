import { useEffect, useMemo, useState } from 'react';
import { Link } from 'react-router-dom';
import { useAppDispatch, useAppSelector } from '@/store';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { fetchUsersThunk } from '@/store/slices/users/usersSlice';
import { selectUsersListMeta } from '@/store/slices/users/selectors';
import { Badge, Card, EmptyState, ErrorMessage, Input, LoadingArea, PageLayout, Pagination, Select, } from '@/components/ui';
import { type UserResponseDto, UserRole } from '@/types';
import { isViewerSubject } from '@/utils/isViewerSubject';
import { PATHS } from '../paths';
const ROLE_LABELS: Record<UserRole, string> = {
    [UserRole.ORGANIZER]: 'Организатор',
    [UserRole.COORDINATOR]: 'Координатор',
    [UserRole.PARTICIPANT]: 'Исполнитель',
    [UserRole.ADMIN]: 'Администратор',
};
const PAGE_SIZE = 20;
export const UsersDirectoryPage = () => {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(selectCurrentUser);
    const list = useAppSelector(selectUsersListMeta);
    const [items, setItems] = useState<UserResponseDto[]>([]);
    const [page, setPage] = useState(1);
    const [search, setSearch] = useState('');
    const [roleFilter, setRoleFilter] = useState<UserRole | ''>('');
    const isAdmin = useMemo(() => currentUser?.roles.includes(UserRole.ADMIN) ?? false, [currentUser?.roles]);
    const roleFilterOptions = useMemo(() => {
        const entries = (Object.entries(ROLE_LABELS) as [
            UserRole,
            string
        ][]).filter(([role]) => isAdmin || role !== UserRole.ADMIN);
        return [
            { value: '', label: 'Все роли' },
            ...entries.map(([value, label]) => ({ value, label })),
        ];
    }, [isAdmin]);
    useEffect(() => {
        if (!isAdmin && roleFilter === UserRole.ADMIN) {
            setRoleFilter('');
            setPage(1);
        }
    }, [isAdmin, roleFilter]);
    useEffect(() => {
        let cancelled = false;
        void dispatch(fetchUsersThunk({
            page,
            size: PAGE_SIZE,
            ...(search.trim() ? { username: search.trim() } : {}),
            ...(roleFilter !== '' ? { role: roleFilter } : {}),
        }))
            .unwrap()
            .then((result) => {
            if (!cancelled)
                setItems(result.items);
        })
            .catch(() => {
            if (!cancelled)
                setItems([]);
        });
        return () => {
            cancelled = true;
        };
    }, [dispatch, page, search, roleFilter]);
    const handleSearchChange = (value: string) => {
        setSearch(value);
        setPage(1);
    };
    const handleRoleFilterChange = (value: string) => {
        setRoleFilter(value === '' ? '' : (value as UserRole));
        setPage(1);
    };
    return (<PageLayout title="Участники" description="Координаторы и исполнители с доступом к системе.">
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      <div className="grid gap-3 md:grid-cols-2">
        <Input label="Поиск по ФИО или логину" placeholder="Введите ФИО или логин пользователя" value={search} onChange={(e) => handleSearchChange(e.target.value)}/>
        <Select label="Роль" options={roleFilterOptions} value={roleFilter} onChange={(e) => handleRoleFilterChange(e.target.value)}/>
      </div>
      {list.status === 'pending' && items.length === 0 ? <LoadingArea /> : null}
      {list.status !== 'pending' && items.length === 0 && !list.error ? (<EmptyState title="Пользователей не найдено"/>) : null}
      <Card padded={false}>
        <ul className="divide-y divide-stroke">
          {items.map((u) => (<li key={u.id}>
              <Link to={isViewerSubject(currentUser?.id, u.id)
                ? PATHS.profile
                : PATHS.userDetail(u.id)} className="flex flex-col gap-2 px-5 py-4 transition-colors hover:bg-secondary/40 sm:flex-row sm:items-center sm:justify-between">
                <div>
                  <p className="font-medium text-headline">{u.username}</p>
                  <p className="text-sm text-paragraph">
                    {isViewerSubject(currentUser?.id, u.id) ? 'Вы' : u.fullName}
                  </p>
                  <p className="text-sm text-paragraph">{u.email}</p>
                </div>
                <div className="flex flex-wrap gap-1.5">
                  {u.roles.map((r) => (<Badge key={r} tone="neutral" outline>
                      {ROLE_LABELS[r] ?? r}
                    </Badge>))}
                </div>
              </Link>
            </li>))}
        </ul>
      </Card>
      <Pagination page={page} totalPages={list.totalPages} onChange={setPage} disabled={list.status === 'pending'}/>
    </PageLayout>);
};
