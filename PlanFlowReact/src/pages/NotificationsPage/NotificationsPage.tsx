import { useEffect, useMemo, useState } from 'react';
import { useSearchParams } from 'react-router-dom';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import Divider from '@mui/material/Divider';
import Chip from '@mui/material/Chip';
import { PageLayout, Card, EmptyState, Button, Pagination, formatDateTime } from '@/components/ui';
import { useAppDispatch, useAppSelector } from '@/store';
import { fetchNotificationsThunk, markAllNotificationsReadThunk, markNotificationReadThunk, } from '@/store/slices/notifications/notificationsSlice';
import { selectNotifications, selectNotificationsListMeta, } from '@/store/slices/notifications/selectors';
export const NotificationsPage = () => {
    const dispatch = useAppDispatch();
    const items = useAppSelector(selectNotifications);
    const listMeta = useAppSelector(selectNotificationsListMeta);
    const [filter, setFilter] = useState<'unread' | 'read' | 'all'>('all');
    const [page, setPage] = useState(1);
    const [searchParams] = useSearchParams();
    const focusId = searchParams.get('focus');
    useEffect(() => {
        void dispatch(fetchNotificationsThunk({ filter, page, size: 20 }));
    }, [dispatch, filter, page]);
    useEffect(() => {
        setPage(1);
    }, [filter]);
    useEffect(() => {
        if (!focusId)
            return;
        const t = window.setTimeout(() => {
            const el = document.getElementById(`notification-${focusId}`);
            el?.scrollIntoView({ block: 'center', behavior: 'smooth' });
        }, 50);
        return () => window.clearTimeout(t);
    }, [focusId, items.length]);
    const filtered = useMemo(() => {
        if (filter === 'all')
            return items;
        if (filter === 'read')
            return items.filter((n) => n.readAt !== null);
        return items.filter((n) => n.readAt === null);
    }, [filter, items]);
    return (<PageLayout title="Уведомления" description="Новые сверху, старые снизу. Прочитанные и непрочитанные отмечены.">
      <Card>
        <Box sx={{ display: 'flex', flexDirection: 'column', gap: 2 }}>
          <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, alignItems: 'center', justifyContent: 'space-between' }}>
            <Box sx={{ display: 'flex', flexWrap: 'wrap', gap: 1, alignItems: 'center' }}>
              <Chip label="Все" color={filter === 'all' ? 'primary' : 'default'} variant={filter === 'all' ? 'filled' : 'outlined'} onClick={() => setFilter('all')}/>
              <Chip label="Непрочитанные" color={filter === 'unread' ? 'primary' : 'default'} variant={filter === 'unread' ? 'filled' : 'outlined'} onClick={() => setFilter('unread')}/>
              <Chip label="Прочитанные" color={filter === 'read' ? 'primary' : 'default'} variant={filter === 'read' ? 'filled' : 'outlined'} onClick={() => setFilter('read')}/>
            </Box>
            <Button size="sm" variant="ghost" onClick={() => void dispatch(markAllNotificationsReadThunk()).then(() => dispatch(fetchNotificationsThunk({ filter, page, size: 20 })))} disabled={items.every((n) => n.readAt !== null) || listMeta.status === 'pending'}>
              Отметить все как прочитанные
            </Button>
          </Box>

          <Divider />

          {filtered.length === 0 ? (<EmptyState title="Уведомлений нет"/>) : (<Box sx={{ display: 'flex', flexDirection: 'column' }}>
              {filtered.map((n, idx) => (<Box key={n.id} id={`notification-${n.id}`}>
                  <Box role="button" tabIndex={0} className="cursor-pointer" onClick={() => {
                    if (n.readAt !== null)
                        return;
                    void dispatch(markNotificationReadThunk({ id: n.id })).then(() => dispatch(fetchNotificationsThunk({ filter, page, size: 20 })));
                }} onKeyDown={(e) => {
                    if (e.key === 'Enter' || e.key === ' ') {
                        e.preventDefault();
                        if (n.readAt !== null)
                            return;
                        void dispatch(markNotificationReadThunk({ id: n.id })).then(() => dispatch(fetchNotificationsThunk({ filter, page, size: 20 })));
                    }
                }} sx={{
                    display: 'flex',
                    flexDirection: 'column',
                    gap: 0.5,
                    py: 1.5,
                    px: 1,
                    borderRadius: 2,
                    '&:hover': { backgroundColor: 'action.hover' },
                }}>
                    <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', justifyContent: 'space-between' }}>
                      <Box sx={{ display: 'flex', gap: 1, alignItems: 'center', minWidth: 0 }}>
                        {n.readAt === null ? (<Chip size="small" color="error" label="Новое"/>) : (<Chip size="small" variant="outlined" label="Прочитано"/>)}
                        {n.title ? (<Typography variant="subtitle2" color="text.primary" sx={{ fontWeight: 600 }}>
                            {n.title}
                          </Typography>) : null}
                      </Box>
                      <Typography variant="caption" color="text.secondary">
                        {formatDateTime(new Date(n.createdAt).toISOString())}
                      </Typography>
                    </Box>
                    <Typography variant="body2" color="text.secondary">
                      {n.message}
                    </Typography>
                  </Box>
                  {idx < filtered.length - 1 ? <Divider /> : null}
                </Box>))}
            </Box>)}

          <Pagination page={page} totalPages={listMeta.totalPages} onChange={setPage} disabled={listMeta.status === 'pending'}/>
        </Box>
      </Card>
    </PageLayout>);
};
