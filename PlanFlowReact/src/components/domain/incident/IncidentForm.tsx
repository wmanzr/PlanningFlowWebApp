import { useEffect, useMemo, useRef, useState } from 'react';
import { useForm, useWatch } from 'react-hook-form';
import { z } from 'zod';
import { Button, Input, Select, Textarea } from '@/components/ui';
import { IncidentSeverity, TaskStatus, asTaskId, type EventId, type IncidentCreateRequest, type TaskId, type TaskResponseDto, type UserId, } from '@/types';
const DESCRIPTION_MIN_LENGTH = 10;
const DESCRIPTION_MAX_LENGTH = 2000;
const SEVERITY_OPTIONS = [
    { value: IncidentSeverity.LOW, label: 'Низкая' },
    { value: IncidentSeverity.MEDIUM, label: 'Средняя' },
    { value: IncidentSeverity.HIGH, label: 'Высокая' },
    { value: IncidentSeverity.CRITICAL, label: 'Критическая' },
];
function buildIncidentSchema(tasks: TaskResponseDto[]) {
    return z
        .object({
        description: z
            .string()
            .trim()
            .min(DESCRIPTION_MIN_LENGTH, `Минимум ${DESCRIPTION_MIN_LENGTH} символов`)
            .max(DESCRIPTION_MAX_LENGTH),
        severity: z
            .string()
            .trim()
            .min(1, 'Выберите критичность')
            .refine((v): v is IncidentSeverity => (Object.values(IncidentSeverity) as readonly string[]).includes(v), { message: 'Выберите критичность' }),
        noTaskAttach: z.coerce.boolean(),
        taskId: z.preprocess((val) => {
            if (val === '' || val === undefined || val === null)
                return undefined;
            const n = typeof val === 'number' ? val : Number(val);
            return Number.isFinite(n) && n > 0 ? n : undefined;
        }, z.number().int().positive().optional()),
    })
        .superRefine((data, ctx) => {
        if (tasks.length === 0) {
            if (!data.noTaskAttach) {
                ctx.addIssue({
                    code: z.ZodIssueCode.custom,
                    path: ['noTaskAttach'],
                    message: 'У мероприятия нет задач — отметьте «Не прикреплять к задаче».',
                });
            }
            return;
        }
        if (data.noTaskAttach)
            return;
        if (data.taskId === undefined) {
            ctx.addIssue({
                code: z.ZodIssueCode.custom,
                path: ['taskId'],
                message: 'Выберите задачу из списка или отметьте «Не прикреплять к задаче»',
            });
        }
    });
}
type FormShape = z.input<ReturnType<typeof buildIncidentSchema>>;
export interface IncidentFormProps {
    reporterId: UserId;
    eventId: EventId;
    tasks: TaskResponseDto[];
    defaultTaskId?: TaskId;
    submitting?: boolean;
    onSubmit: (body: IncidentCreateRequest) => void;
    onCancel?: () => void;
}
export const IncidentForm = ({ reporterId, eventId, tasks, defaultTaskId, submitting, onSubmit, onCancel, }: IncidentFormProps) => {
    const attachableTasks = useMemo(() => tasks.filter((t) => t.status !== TaskStatus.DONE && t.status !== TaskStatus.CANCELLED), [tasks]);
    const defaultTask = useMemo(() => defaultTaskId !== undefined
        ? attachableTasks.find((t) => t.id === defaultTaskId)
        : undefined, [attachableTasks, defaultTaskId]);
    const { register, handleSubmit, control, setValue, setError, clearErrors, formState: { errors }, } = useForm<FormShape>({
        defaultValues: {
            description: '',
            severity: '',
            noTaskAttach: attachableTasks.length === 0,
            taskId: defaultTask !== undefined ? defaultTask.id : undefined,
        },
    });
    const noTaskAttach = useWatch({ control, name: 'noTaskAttach' });
    const taskId = useWatch({ control, name: 'taskId' }) as TaskId | undefined;
    const [taskQuery, setTaskQuery] = useState(defaultTask?.title ?? '');
    const [taskListOpen, setTaskListOpen] = useState(false);
    const taskPickerRef = useRef<HTMLDivElement>(null);
    useEffect(() => {
        if (noTaskAttach) {
            setValue('taskId', undefined, { shouldValidate: false });
            setTaskQuery('');
            setTaskListOpen(false);
        }
    }, [noTaskAttach, setValue]);
    useEffect(() => {
        if (taskId === undefined)
            return;
        const t = attachableTasks.find((x) => x.id === taskId);
        if (t)
            setTaskQuery(t.title);
    }, [taskId, attachableTasks]);
    useEffect(() => {
        if (attachableTasks.length === 0) {
            setValue('noTaskAttach', true, { shouldValidate: false });
        }
    }, [attachableTasks.length, setValue]);
    useEffect(() => {
        const onDoc = (e: MouseEvent) => {
            if (!taskPickerRef.current?.contains(e.target as Node)) {
                setTaskListOpen(false);
            }
        };
        document.addEventListener('mousedown', onDoc);
        return () => document.removeEventListener('mousedown', onDoc);
    }, []);
    const duplicateTitleKeys = useMemo(() => {
        const counts = new Map<string, number>();
        for (const t of attachableTasks) {
            const k = t.title.trim().toLowerCase();
            counts.set(k, (counts.get(k) ?? 0) + 1);
        }
        const dups = new Set<string>();
        for (const [k, n] of counts) {
            if (n > 1)
                dups.add(k);
        }
        return dups;
    }, [attachableTasks]);
    const filteredTasks = useMemo(() => {
        const q = taskQuery.trim().toLowerCase();
        if (!q)
            return [];
        const list = [...attachableTasks].sort((a, b) => a.title.localeCompare(b.title, 'ru'));
        return list.filter((t) => t.title.toLowerCase().includes(q)).slice(0, 40);
    }, [attachableTasks, taskQuery]);
    const submit = handleSubmit((raw) => {
        clearErrors();
        const parsed = buildIncidentSchema(attachableTasks).safeParse(raw);
        if (!parsed.success) {
            for (const issue of parsed.error.issues) {
                const p = issue.path[0];
                if (typeof p === 'string') {
                    setError(p as keyof FormShape, { message: issue.message });
                }
            }
            return;
        }
        const data = parsed.data;
        onSubmit({
            reporterId,
            eventId,
            description: data.description,
            severity: data.severity as IncidentSeverity,
            ...(!data.noTaskAttach && data.taskId !== undefined ? { taskId: asTaskId(data.taskId) } : {}),
        });
    });
    const hasTasks = attachableTasks.length > 0;
    return (<form className="flex flex-col gap-4" onSubmit={submit} noValidate>
      <input type="hidden" {...register('taskId', { valueAsNumber: true })}/>

      <Textarea label="Описание" rows={4} error={errors.description?.message} {...register('description')}/>
      <div className="grid gap-4 md:grid-cols-2">
        <Select label="Критичность" placeholder="Выберите критичность" options={SEVERITY_OPTIONS} error={errors.severity?.message} {...register('severity')}/>
      </div>

      <div className="flex flex-col gap-1">
        <label className="flex cursor-pointer items-center gap-2 text-sm text-headline">
          <input type="checkbox" className="h-4 w-4 rounded border-secondary" {...register('noTaskAttach')}/>
          Не прикреплять к задаче
        </label>
        {errors.noTaskAttach?.message ? (<p className="text-sm text-danger">{errors.noTaskAttach.message}</p>) : null}
      </div>

      {!noTaskAttach ? (<div ref={taskPickerRef} className="relative flex flex-col gap-1">
          {taskListOpen && hasTasks && taskQuery.trim().length > 0 ? (<ul className="absolute bottom-full left-0 right-0 z-20 mb-1 max-h-52 overflow-y-auto rounded-md border border-secondary/60 bg-bg shadow-lg" role="listbox">
              {filteredTasks.length === 0 ? (<li className="border-b border-secondary/40 px-3 py-2 text-sm text-paragraph last:border-b-0">
                  Нет совпадений
                </li>) : (filteredTasks.map((t) => (<li key={t.id} className="border-b border-secondary/40 last:border-b-0">
                    <button type="button" className="flex w-full min-w-0 items-center justify-between gap-2 px-3 py-2 text-left text-sm hover:bg-surface-muted" onMouseDown={(e) => {
                        e.preventDefault();
                        setValue('taskId', t.id, { shouldValidate: false });
                        setTaskQuery(t.title);
                        setTaskListOpen(false);
                    }}>
                      <span className="min-w-0 truncate">{t.title}</span>
                      {duplicateTitleKeys.has(t.title.trim().toLowerCase()) ? (<span className="shrink-0 text-xs tabular-nums text-paragraph">№{t.id}</span>) : null}
                    </button>
                  </li>)))}
            </ul>) : null}
          <Input label="Задача" placeholder="Начните вводить название — появится список" value={taskQuery} onChange={(e) => {
                const v = e.target.value;
                setTaskQuery(v);
                setValue('taskId', undefined, { shouldValidate: false });
                setTaskListOpen(v.trim().length > 0);
            }} onFocus={() => {
                if (taskQuery.trim().length > 0)
                    setTaskListOpen(true);
            }} error={errors.taskId?.message} autoComplete="off" disabled={!hasTasks}/>
          {!hasTasks ? (<p className="text-xs text-paragraph">
              {tasks.length === 0
                    ? 'На мероприятии нет задач — отметьте «Не прикреплять к задаче».'
                    : 'Нет активных задач — отметьте «Не прикреплять к задаче».'}
            </p>) : null}
        </div>) : null}

      <div className="flex justify-end gap-2">
        {onCancel ? (<Button type="button" variant="ghost" onClick={onCancel}>
            Отмена
          </Button>) : null}
        <Button type="submit" loading={submitting}>
          Создать инцидент
        </Button>
      </div>
    </form>);
};
