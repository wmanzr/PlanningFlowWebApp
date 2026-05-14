import { useEffect, useMemo, useState } from 'react';
import { useNavigate, useParams } from 'react-router-dom';
import { useForm } from 'react-hook-form';
import { z } from 'zod';
import Typography from '@mui/material/Typography';
import { useAppDispatch, useAppSelector } from '@/store';
import { acceptIncidentThunk, fetchIncidentByIdThunk, incidentsActions, resolveIncidentThunk, } from '@/store/slices/incidents/incidentsSlice';
import { makeSelectIncidentById, selectIncidentsActionMeta, } from '@/store/slices/incidents/selectors';
import { fetchEventByIdThunk } from '@/store/slices/events/eventsSlice';
import { selectEventById } from '@/store/slices/events/selectors';
import { Button, Card, ErrorMessage, LoadingArea, Modal, PageLayout, Textarea, formatDateTime, } from '@/components/ui';
import { IncidentRow } from '@/components/domain/incident';
import { IncidentStatus, asIncidentId } from '@/types';
const RESOLUTION_MIN_LENGTH = 5;
const RESOLUTION_MAX_LENGTH = 1000;
const resolveSchema = z.object({
    resolutionNotes: z
        .string()
        .trim()
        .min(RESOLUTION_MIN_LENGTH, `Минимум ${RESOLUTION_MIN_LENGTH} символов`)
        .max(RESOLUTION_MAX_LENGTH),
});
type ResolveValues = z.infer<typeof resolveSchema>;
export const IncidentDetailPage = () => {
    const params = useParams<{
        incidentId: string;
    }>();
    const incidentId = useMemo(() => {
        const num = Number.parseInt(params.incidentId ?? '', 10);
        return Number.isFinite(num) ? asIncidentId(num) : undefined;
    }, [params.incidentId]);
    const dispatch = useAppDispatch();
    const navigate = useNavigate();
    const selectIncident = useMemo(() => makeSelectIncidentById(incidentId), [incidentId]);
    const incident = useAppSelector(selectIncident);
    const event = useAppSelector(selectEventById(incident?.eventId));
    const action = useAppSelector(selectIncidentsActionMeta);
    const [loadFailed, setLoadFailed] = useState(false);
    const [isResolveOpen, setIsResolveOpen] = useState(false);
    const resolveForm = useForm<ResolveValues>({ defaultValues: { resolutionNotes: '' } });
    useEffect(() => {
        if (incidentId === undefined)
            return;
        setLoadFailed(false);
        void dispatch(fetchIncidentByIdThunk(incidentId))
            .unwrap()
            .catch(() => setLoadFailed(true));
    }, [dispatch, incidentId]);
    useEffect(() => {
        if (incident?.eventId !== undefined) {
            void dispatch(fetchEventByIdThunk(incident.eventId));
        }
    }, [dispatch, incident?.eventId]);
    if (incidentId === undefined) {
        return (<PageLayout title="Инцидент">
        <ErrorMessage message="Некорректный идентификатор инцидента"/>
      </PageLayout>);
    }
    if (loadFailed && !incident) {
        return (<PageLayout title="Инцидент">
        <ErrorMessage message="Инцидент не найден или нет доступа"/>
      </PageLayout>);
    }
    if (!incident) {
        return <LoadingArea />;
    }
    const handleResolve = resolveForm.handleSubmit((values) => {
        const parsed = resolveSchema.safeParse(values);
        if (!parsed.success)
            return;
        void dispatch(resolveIncidentThunk({
            id: incident.id,
            body: { resolutionNotes: parsed.data.resolutionNotes },
        })).then((result) => {
            if (resolveIncidentThunk.fulfilled.match(result)) {
                setIsResolveOpen(false);
                resolveForm.reset({ resolutionNotes: '' });
                void dispatch(fetchIncidentByIdThunk(incident.id));
            }
        });
    });
    return (<PageLayout title="Инцидент" description={event ? `Мероприятие «${event.title}»` : undefined} actions={<Button variant="ghost" onClick={() => navigate(-1)}>
          Назад
        </Button>}>
      {action.error ? (<ErrorMessage message={action.error.message} onShown={() => dispatch(incidentsActions.clearActionError())}/>) : null}

      <Card>
        <IncidentRow incident={incident}/>
      </Card>

      {incident.status !== IncidentStatus.RESOLVED ? (<div className={`mt-6 flex w-full min-w-0 flex-wrap items-center gap-3 ${incident.status === IncidentStatus.OPEN ? 'justify-between' : 'justify-end'}`}>
          {incident.status === IncidentStatus.OPEN ? (<Button variant="secondary" loading={action.status === 'pending'} onClick={() => void dispatch(acceptIncidentThunk(incident.id)).then((r) => {
                    if (acceptIncidentThunk.fulfilled.match(r)) {
                        void dispatch(fetchIncidentByIdThunk(incident.id));
                    }
                })}>
              Начать работу над инцидентом
            </Button>) : null}
          <Button variant="secondary" onClick={() => setIsResolveOpen(true)}>
            Закрыть инцидент
          </Button>
        </div>) : (<Typography variant="body2" color="text.secondary" className="mt-6">
          Инцидент закрыт
          {incident.resolvedAt ? ` • ${formatDateTime(incident.resolvedAt)}` : ''}
        </Typography>)}

      <Modal open={isResolveOpen} onClose={() => {
            setIsResolveOpen(false);
            resolveForm.reset({ resolutionNotes: '' });
        }} title="Закрытие инцидента" footer={<>
            <Button variant="ghost" onClick={() => {
                setIsResolveOpen(false);
                resolveForm.reset({ resolutionNotes: '' });
            }}>
              Отмена
            </Button>
            <Button onClick={handleResolve} loading={action.status === 'pending'}>
              Закрыть
            </Button>
          </>}>
        <Textarea label="Резолюция" rows={4} error={resolveForm.formState.errors.resolutionNotes?.message} {...resolveForm.register('resolutionNotes')}/>
      </Modal>
    </PageLayout>);
};
