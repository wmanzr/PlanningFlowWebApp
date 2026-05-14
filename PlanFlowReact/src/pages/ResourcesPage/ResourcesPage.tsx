import { useEffect, useMemo, useState } from 'react';
import { useAppDispatch, useAppSelector } from '@/store';
import { selectCurrentUser } from '@/store/slices/auth/selectors';
import { createInternalResourceThunk, deleteInternalResourceThunk, fetchInternalResourcesThunk, markResourceBrokenThunk, markResourceOperationalThunk, resourcesActions, updateInternalResourceThunk, } from '@/store/slices/resources/resourcesSlice';
import { selectAllResources, selectResourcesActionMeta, selectResourcesListMeta, } from '@/store/slices/resources/selectors';
import { Button, Card, CardHeader, EmptyState, ErrorMessage, Input, LoadingArea, Modal, PageLayout, Pagination, Select, } from '@/components/ui';
import { InternalResourceForm, RESOURCE_TYPE_LABEL, ResourceRow, } from '@/components/domain/resource';
import { ResourceType, UserRole, type InternalResourceCreateRequest, type InternalResourceResponseDto, type InternalResourceUpdateRequest, } from '@/types';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
const PAGE_SIZE = 20;
export const ResourcesPage = () => {
    const dispatch = useAppDispatch();
    const currentUser = useAppSelector(selectCurrentUser);
    const resources = useAppSelector(selectAllResources);
    const list = useAppSelector(selectResourcesListMeta);
    const action = useAppSelector(selectResourcesActionMeta);
    const canCreateInternalResource = useMemo(() => Boolean(currentUser?.roles.includes(UserRole.ADMIN) ||
        currentUser?.roles.includes(UserRole.ORGANIZER)), [currentUser?.roles]);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(1);
    const [typeFilter, setTypeFilter] = useState<'' | ResourceType>('');
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const [editTarget, setEditTarget] = useState<InternalResourceResponseDto | null>(null);
    useEffect(() => {
        setPage(1);
    }, [search]);
    useEffect(() => {
        const q = search.trim();
        void dispatch(fetchInternalResourcesThunk({ page, size: PAGE_SIZE, ...(q ? { name: q } : {}) }));
    }, [dispatch, page, search]);
    const filteredResources = useMemo(() => {
        if (!typeFilter)
            return resources;
        return resources.filter((r) => r.type === typeFilter);
    }, [resources, typeFilter]);
    const handleCreate = (body: InternalResourceCreateRequest | InternalResourceUpdateRequest) => {
        if (!('name' in body) || !('inventoryNumber' in body) || !('type' in body))
            return;
        if (typeof body.name !== 'string' ||
            typeof body.inventoryNumber !== 'string' ||
            body.type === undefined) {
            return;
        }
        void dispatch(createInternalResourceThunk({
            name: body.name,
            inventoryNumber: body.inventoryNumber,
            type: body.type,
        })).then((result) => {
            if (createInternalResourceThunk.fulfilled.match(result)) {
                setIsCreateOpen(false);
                setPage(1);
                void dispatch(fetchInternalResourcesThunk({
                    page: 1,
                    size: PAGE_SIZE,
                    ...(search.trim() ? { name: search.trim() } : {}),
                }));
            }
        });
    };
    const handleEdit = (body: InternalResourceCreateRequest | InternalResourceUpdateRequest) => {
        if (!editTarget)
            return;
        void dispatch(updateInternalResourceThunk({ id: editTarget.id, body })).then((result) => {
            if (updateInternalResourceThunk.fulfilled.match(result)) {
                setEditTarget(null);
            }
        });
    };
    return (<PageLayout title="Внутренние ресурсы" description="Каталог ресурсов организации, доступных для резервирования." actions={canCreateInternalResource ? (<Button onClick={() => setIsCreateOpen(true)}>Добавить ресурс</Button>) : undefined}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(resourcesActions.clearActionError())}/>) : null}
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      <div className="grid gap-3 md:grid-cols-2">
        <Input label="Поиск по названию" placeholder="например, проектор" value={search} onChange={(e) => setSearch(e.target.value)}/>
        <Select label="Тип ресурса" options={[
            { value: '', label: 'Все типы' },
            ...(Object.entries(RESOURCE_TYPE_LABEL) as [
                ResourceType,
                string
            ][]).map(([value, label]) => ({ value, label })),
        ]} value={typeFilter} onChange={(e) => setTypeFilter((e.target.value || '') as '' | ResourceType)}/>
      </div>
      {list.status === 'pending' && resources.length === 0 ? <LoadingArea /> : null}
      {filteredResources.length === 0 && list.status !== 'pending' ? (<EmptyState title="Ресурсов не найдено"/>) : null}
      {filteredResources.length > 0 ? (<Card padded={false}>
          <div className="flex flex-col gap-3 p-5">
            {filteredResources.map((resource) => (<ResourceRow key={resource.id} resource={resource} actions={<>
                    <div className="flex flex-nowrap items-center justify-end gap-2">
                      <Button size="sm" variant="ghost" type="button" onClick={() => setEditTarget(resource)}>
                        Редактировать
                      </Button>
                      <Button size="icon" variant="ghost" type="button" title="Удалить ресурс" aria-label="Удалить ресурс" className="border border-slate-400/80 text-slate-600 hover:border-red-400 hover:bg-red-500/10 hover:text-red-700" onClick={() => {
                        if (!window.confirm(`Удалить ресурс «${resource.name}» (инв. № ${resource.inventoryNumber})? Связанные бронирования будут удалены.`)) {
                            return;
                        }
                        void dispatch(deleteInternalResourceThunk(resource.id));
                    }}>
                        <DeleteOutlineOutlinedIcon sx={{ fontSize: 22 }}/>
                      </Button>
                    </div>
                    <div className="flex flex-wrap justify-end">
                      {resource.operational ? (<Button size="sm" variant="ghost" type="button" className="border border-amber-500/55 bg-amber-50 text-amber-950 hover:border-amber-600 hover:bg-amber-100" onClick={() => dispatch(markResourceBrokenThunk(resource.id))}>
                          Отметить как сломанный
                        </Button>) : (<Button size="sm" variant="ghost" type="button" className="border border-emerald-500/50 bg-emerald-50 text-emerald-950 hover:border-emerald-600 hover:bg-emerald-100" onClick={() => dispatch(markResourceOperationalThunk(resource.id))}>
                          Восстановлен
                        </Button>)}
                    </div>
                  </>}/>))}
          </div>
        </Card>) : null}

      <div className="mt-4">
        <Pagination page={page} totalPages={list.totalPages} onChange={setPage} disabled={list.status === 'pending'}/>
      </div>

      <Modal open={isCreateOpen && canCreateInternalResource} onClose={() => setIsCreateOpen(false)} title="Новый внутренний ресурс">
        <CardHeader title="Параметры"/>
        <InternalResourceForm submitting={action.status === 'pending'} onCancel={() => setIsCreateOpen(false)} onSubmit={handleCreate}/>
      </Modal>

      <Modal open={editTarget !== null} onClose={() => setEditTarget(null)} title="Редактирование ресурса">
        {editTarget ? (<InternalResourceForm initial={editTarget} submitting={action.status === 'pending'} onCancel={() => setEditTarget(null)} onSubmit={handleEdit}/>) : null}
      </Modal>
    </PageLayout>);
};
