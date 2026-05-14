import { useEffect, useMemo, useState } from 'react';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import { useAppDispatch, useAppSelector } from '@/store';
import { createSkillThunk, deleteSkillThunk, fetchSkillCategoriesThunk, fetchSkillsThunk, skillsActions, } from '@/store/slices/skills/skillsSlice';
import { selectAllSkills, selectSkillsActionMeta, selectSkillsCategories, selectSkillsListMeta, } from '@/store/slices/skills/selectors';
import { Badge, Button, Card, EmptyState, ErrorMessage, Input, LoadingArea, Modal, PageLayout, Pagination, Select, } from '@/components/ui';
import DeleteOutlineOutlinedIcon from '@mui/icons-material/DeleteOutlineOutlined';
const NAME_MIN_LENGTH = 2;
const NAME_MAX_LENGTH = 200;
const CATEGORY_MIN_LENGTH = 2;
const CATEGORY_MAX_LENGTH = 100;
const schema = z.object({
    name: z
        .string()
        .trim()
        .min(NAME_MIN_LENGTH, `Минимум ${NAME_MIN_LENGTH} символа`)
        .max(NAME_MAX_LENGTH),
    category: z
        .string()
        .trim()
        .min(CATEGORY_MIN_LENGTH, `Минимум ${CATEGORY_MIN_LENGTH} символа`)
        .max(CATEGORY_MAX_LENGTH),
});
type Values = z.infer<typeof schema>;
const PAGE_SIZE = 20;
export const SkillsPage = () => {
    const dispatch = useAppDispatch();
    const skills = useAppSelector(selectAllSkills);
    const list = useAppSelector(selectSkillsListMeta);
    const action = useAppSelector(selectSkillsActionMeta);
    const categories = useAppSelector(selectSkillsCategories);
    const [search, setSearch] = useState('');
    const [page, setPage] = useState(1);
    const [categoryFilter, setCategoryFilter] = useState<string>('');
    const [isCreateOpen, setIsCreateOpen] = useState(false);
    const { register, handleSubmit, formState, reset } = useForm<Values>({
        defaultValues: { name: '', category: '' },
    });
    useEffect(() => {
        void dispatch(fetchSkillCategoriesThunk());
    }, [dispatch]);
    useEffect(() => {
        setPage(1);
    }, [search]);
    useEffect(() => {
        const q = search.trim();
        void dispatch(fetchSkillsThunk({ page, size: PAGE_SIZE, ...(q ? { name: q } : {}) }));
    }, [dispatch, page, search]);
    const filtered = useMemo(() => categoryFilter ? skills.filter((skill) => skill.category === categoryFilter) : skills, [skills, categoryFilter]);
    const groupedByCategory = useMemo(() => {
        const map = new Map<string, typeof skills>();
        for (const s of filtered) {
            const key = (s.category ?? '').trim() || 'Без категории';
            const arr = map.get(key);
            if (arr)
                arr.push(s);
            else
                map.set(key, [s]);
        }
        const entries = [...map.entries()].sort((a, b) => a[0].localeCompare(b[0], 'ru'));
        return entries.map(([category, items]) => ({
            category,
            items: [...items].sort((a, b) => a.name.localeCompare(b.name, 'ru')),
        }));
    }, [filtered, skills]);
    const onCreate = handleSubmit(async (values) => {
        const parsed = schema.safeParse(values);
        if (!parsed.success)
            return;
        const result = await dispatch(createSkillThunk(parsed.data));
        if (createSkillThunk.fulfilled.match(result)) {
            setIsCreateOpen(false);
            reset();
            setPage(1);
            const q = search.trim();
            void dispatch(fetchSkillsThunk({ page: 1, size: PAGE_SIZE, ...(q ? { name: q } : {}) }));
            void dispatch(fetchSkillCategoriesThunk());
        }
    });
    return (<PageLayout title="Справочник навыков" description="Используется при подборе персонала на задачи." actions={<Button onClick={() => setIsCreateOpen(true)}>Добавить навык</Button>}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(skillsActions.clearActionError())}/>) : null}
      {list.error ? <ErrorMessage message={list.error.message}/> : null}
      <div className="grid gap-3 md:grid-cols-2">
        <Input label="Поиск по названию" placeholder="Введите название навыка" value={search} onChange={(e) => setSearch(e.target.value)}/>
        <Select label="Категория" options={[
            { value: '', label: 'Все категории' },
            ...categories.data.map((c) => ({ value: c, label: c })),
        ]} value={categoryFilter} onChange={(e) => setCategoryFilter(e.target.value)}/>
      </div>
      {list.status === 'pending' && skills.length === 0 ? <LoadingArea /> : null}
      {filtered.length === 0 && list.status !== 'pending' ? (<EmptyState title="Навыков не найдено"/>) : null}
      <div className="grid gap-3">
        {groupedByCategory.map((group) => (<div key={group.category} className="grid gap-2">
            <div className="flex items-center justify-between gap-3">
              <h2 className="text-base font-semibold text-headline md:text-lg md:font-bold">
                {group.category}
              </h2>
              <Badge tone="neutral">{group.items.length}</Badge>
            </div>
            <div className="grid gap-3">
              {group.items.map((skill) => (<Card key={skill.id} className="group transition-colors duration-200 hover:bg-secondary/30">
                  <div className="flex items-start justify-between gap-3">
                    <div className="min-w-0 flex-1">
                      <h3 className="text-base font-semibold text-headline">{skill.name}</h3>
                    </div>
                    <div className="pointer-events-none flex shrink-0 translate-x-1 opacity-0 transition-all duration-200 ease-out group-hover:pointer-events-auto group-hover:translate-x-0 group-hover:opacity-100">
                      <Button size="icon" variant="ghost" type="button" title="Удалить навык из справочника" aria-label="Удалить навык из справочника" className="border border-slate-400/80 text-slate-600 hover:border-red-400 hover:bg-red-500/10 hover:text-red-700" onClick={() => {
                    if (!window.confirm(`Удалить навык «${skill.name}» из справочника? Связи с задачами и профилями будут сняты.`)) {
                        return;
                    }
                    void dispatch(deleteSkillThunk(skill.id)).then((result) => {
                        if (deleteSkillThunk.fulfilled.match(result)) {
                            void dispatch(fetchSkillCategoriesThunk());
                            const q = search.trim();
                            void dispatch(fetchSkillsThunk({ page, size: PAGE_SIZE, ...(q ? { name: q } : {}) }));
                        }
                    });
                }}>
                        <DeleteOutlineOutlinedIcon sx={{ fontSize: 22 }}/>
                      </Button>
                    </div>
                  </div>
                </Card>))}
            </div>
          </div>))}
      </div>

      <div className="mt-4">
        <Pagination page={page} totalPages={list.totalPages} onChange={setPage} disabled={list.status === 'pending'}/>
      </div>

      <Modal open={isCreateOpen} onClose={() => {
            setIsCreateOpen(false);
            reset();
        }} title="Новый навык">
        <form className="flex flex-col gap-4" onSubmit={onCreate} noValidate>
          <Input label="Название" error={formState.errors.name?.message} {...register('name')}/>
          <Input label="Категория" error={formState.errors.category?.message} {...register('category')}/>
          <div className="flex justify-end gap-2">
            <Button type="button" variant="ghost" onClick={() => {
            setIsCreateOpen(false);
            reset();
        }}>
              Отмена
            </Button>
            <Button type="submit">Создать</Button>
          </div>
        </form>
      </Modal>
    </PageLayout>);
};
