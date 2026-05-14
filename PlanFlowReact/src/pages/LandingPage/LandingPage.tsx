import { useEffect, useMemo, useRef, useState } from 'react';
import { Link, useNavigate } from 'react-router-dom';
import IconButton from '@mui/material/IconButton';
import { useTheme } from '@mui/material/styles';
import DarkModeOutlinedIcon from '@mui/icons-material/DarkModeOutlined';
import LightModeOutlinedIcon from '@mui/icons-material/LightModeOutlined';
import { useAppDispatch, useAppSelector } from '@/store';
import { uiActions } from '@/store/slices/ui/uiSlice';
import { selectTheme } from '@/store/slices/ui/selectors';
import { publicApi, type PublicLandingStatsDto } from '@/api';
import { Button } from '@/components/ui';
import { PATHS } from '../paths';

const CONTACT_EMAIL = (import.meta.env.VITE_PUBLIC_CONTACT_EMAIL as string | undefined)?.trim() || 'contact@planflow.app';
const PROJECT_NOTE = (import.meta.env.VITE_PUBLIC_PROJECT_BYLINE as string | undefined)?.trim() ||
    'PlanningFlow — учёт мероприятий, задач и назначений для команд, которые работают на площадке и в офисе.';

const EMPTY_STATS: PublicLandingStatsDto = {
    totalEventsCount: 0,
    completedEventsCount: 0,
    tasksDoneCount: 0,
    registeredUsersCount: 0,
    resolvedIncidentsCount: 0,
    acceptedAssignmentsCount: 0,
};

function useAnimatedCount(target: number, durationMs: number) {
    const [display, setDisplay] = useState(0);
    const fromRef = useRef(0);
    useEffect(() => {
        const from = fromRef.current;
        let start: number | null = null;
        let frame: number;
        const step = (t: number) => {
            if (start === null) {
                start = t;
            }
            const p = Math.min(1, (t - start) / durationMs);
            const eased = 1 - Math.pow(1 - p, 3);
            const next = Math.round(from + (target - from) * eased);
            setDisplay(next);
            if (p < 1) {
                frame = requestAnimationFrame(step);
            }
            else {
                fromRef.current = target;
            }
        };
        frame = requestAnimationFrame(step);
        return () => cancelAnimationFrame(frame);
    }, [target, durationMs]);
    return display;
}

const nf = new Intl.NumberFormat('ru-RU');

export const LandingPage = () => {
    const navigate = useNavigate();
    const dispatch = useAppDispatch();
    const mode = useAppSelector(selectTheme);
    const theme = useTheme();
    const app = theme.app;
    const [stats, setStats] = useState<PublicLandingStatsDto>(EMPTY_STATS);
    const [statsReady, setStatsReady] = useState(false);
    useEffect(() => {
        let cancelled = false;
        void publicApi
            .landingStats()
            .then((s) => {
            if (!cancelled) {
                setStats(s);
                setStatsReady(true);
            }
        })
            .catch(() => {
            if (!cancelled) {
                setStats(EMPTY_STATS);
                setStatsReady(true);
            }
        });
        return () => {
            cancelled = true;
        };
    }, []);
    const t = statsReady ? stats : EMPTY_STATS;
    const dur = 1250;
    const aTotal = useAnimatedCount(statsReady ? t.totalEventsCount : 0, dur);
    const aDone = useAnimatedCount(statsReady ? t.completedEventsCount : 0, dur);
    const aTasks = useAnimatedCount(statsReady ? t.tasksDoneCount : 0, dur);
    const aUsers = useAnimatedCount(statsReady ? t.registeredUsersCount : 0, dur);
    const aInc = useAnimatedCount(statsReady ? t.resolvedIncidentsCount : 0, dur);
    const aAsg = useAnimatedCount(statsReady ? t.acceptedAssignmentsCount : 0, dur);
    const gradientOrbs = useMemo(() => ({
        a: app.highlight,
        b: app.accent,
        c: mode === 'dark' ? 'rgba(167, 139, 250, 0.35)' : 'rgba(98, 70, 234, 0.28)',
    }), [app.highlight, app.accent, mode]);
    const statRows: {
        label: string;
        value: number;
    }[] = [
        { label: 'Создано мероприятий', value: aTotal },
        { label: 'Мероприятий завершено', value: aDone },
        { label: 'Выполнено задач', value: aTasks },
        { label: 'Пользователей зарегистрировано', value: aUsers },
        { label: 'Инцидентов закрыто', value: aInc },
        { label: 'Принятых назначений', value: aAsg },
    ];
    return (<div className="snap-y snap-mandatory overflow-y-auto overflow-x-hidden scroll-smooth" style={{
            height: '100dvh',
            backgroundColor: app.bg,
        }}>
      <section className="relative flex min-h-[100dvh] snap-start flex-col">
        <div className="pointer-events-none absolute inset-0 overflow-hidden">
          <div className="animate-landing-drift absolute -left-[20%] top-[10%] h-[min(52vw,420px)] w-[min(52vw,420px)] rounded-full blur-[100px]" style={{
                background: gradientOrbs.a,
                opacity: 0.35,
            }}/>
          <div className="animate-landing-drift-slow absolute -right-[15%] top-[35%] h-[min(48vw,380px)] w-[min(48vw,380px)] rounded-full blur-[90px]" style={{
                background: gradientOrbs.b,
                opacity: 0.28,
            }}/>
          <div className="animate-landing-pulse-soft absolute left-[25%] bottom-[5%] h-[min(40vw,320px)] w-[min(55vw,440px)] rounded-full blur-[110px]" style={{
                background: gradientOrbs.c,
            }}/>
        </div>

        <header className="relative z-10 flex shrink-0 items-center justify-between px-5 pt-4 md:px-10">
          <span className="text-sm font-semibold tracking-tight text-headline md:text-base">
            PlanningFlow
          </span>
          <IconButton onClick={() => dispatch(uiActions.toggleTheme())} aria-label="Переключить тему" size="small" sx={{
                border: `1px solid ${mode === 'dark' ? 'rgba(148,161,178,0.35)' : 'rgba(43,44,52,0.12)'}`,
                backgroundColor: mode === 'dark' ? 'rgba(31,33,40,0.6)' : 'rgba(255,255,254,0.7)',
                backdropFilter: 'blur(10px)',
            }}>
            {mode === 'dark' ? <LightModeOutlinedIcon fontSize="small"/> : <DarkModeOutlinedIcon fontSize="small"/>}
          </IconButton>
        </header>

        <div className="relative z-10 mx-auto flex w-full max-w-6xl flex-1 flex-col justify-center px-5 pb-6 pt-4 md:px-10 md:pb-10 md:pt-6">
          <div className="mx-auto max-w-3xl text-center">
            <p className="mb-2 text-[11px] font-medium uppercase tracking-[0.2em] text-paragraph/75 md:text-xs">
              События · Задачи · Ресурсы
            </p>
            <h1 className="text-balance bg-gradient-to-b from-headline via-headline to-headline/75 bg-clip-text text-[1.55rem] font-semibold leading-tight tracking-tight text-transparent md:text-[2.55rem] md:leading-[1.12]">
              Одно приложение для подготовки мероприятия и контроля задач
            </h1>
            <p className="mx-auto mt-3 max-w-2xl text-pretty text-sm leading-relaxed text-paragraph/90 md:mt-4 md:text-[15px]">
                Один план — одна система. Все задачи, люди и ресурсы синхронизированы и всегда актуальны.
            </p>
          </div>

          <div className="mx-auto mt-6 grid max-w-5xl gap-2.5 sm:grid-cols-3 md:mt-8 md:gap-3">
            {[{
                t: 'Команда под контролем',
                d: 'Назначайте людей на задачи и сразу видите, кто за что отвечает. Без хаоса в чатах.',
            }, {
                t: 'План и выполнение в одном месте',
                d: 'Все задачи, сроки и статусы — внутри мероприятия. Понятно, что сделано и что требует отдельного внимания.',
            }, {
                t: 'Ресурсы и инциденты',
                d: 'Бронирования и учёт сбоев привязаны к событию и задачам, чтобы быстрее восстановить работу.',
            }].map((item) => (<div key={item.t} className="rounded-lg border border-secondary/40 bg-surface/50 px-3 py-2.5 text-left shadow-card backdrop-blur-md md:px-4 md:py-3">
                <h3 className="text-[13px] font-semibold text-headline md:text-sm">{item.t}</h3>
                <p className="mt-1 text-[11px] leading-relaxed text-paragraph/88 md:text-xs">{item.d}</p>
              </div>))}
          </div>

          <div className="mx-auto mt-6 grid w-full max-w-5xl grid-cols-2 gap-2 sm:grid-cols-3 md:mt-8 md:gap-3">
            {statRows.map((row) => (<div key={row.label} className="rounded-xl border border-secondary/45 bg-surface-muted/55 px-3 py-2.5 backdrop-blur-md md:py-3">
                <p className="text-[10px] font-medium uppercase leading-snug tracking-wide text-paragraph/70 md:text-[11px]">
                  {row.label}
                </p>
                <p className="mt-1 font-[inherit] text-xl font-semibold tabular-nums tracking-tight text-headline md:text-2xl" style={{
                    fontFeatureSettings: '"tnum"',
                }}>
                  {statsReady ? nf.format(row.value) : '…'}
                </p>
              </div>))}
          </div>

          <div className="mx-auto mt-7 flex max-w-xl flex-col items-center md:mt-9">
            <Button size="lg" className="!min-h-[52px] !rounded-full !px-12 !py-3 !text-lg font-semibold !leading-snug shadow-xl md:!min-h-[58px] md:!px-16 md:!py-4 md:!text-xl" style={{
                background: `linear-gradient(135deg, ${app.button} 0%, ${app.accent} 100%)`,
                boxShadow: `0 16px 48px ${app.accentSoft}`,
            }} onClick={() => navigate(PATHS.auth)}>
              Начать планирование
            </Button>
            <p className="mt-4 text-center text-xs text-paragraph/80 md:text-sm">
              Нет аккаунта?{' '}
              <Link to={PATHS.register} className="font-medium text-highlight underline-offset-4 hover:underline">
                Регистрация
              </Link>
            </p>
          </div>
        </div>
      </section>

      <footer id="landing-footer" className="snap-start border-t border-secondary/35 bg-surface/80 px-5 py-10 backdrop-blur-md md:px-10">
        <div className="mx-auto flex max-w-3xl flex-col gap-6 text-center md:flex-row md:items-start md:justify-between md:text-left">
          <div>
            <p className="text-xs font-semibold uppercase tracking-wider text-paragraph/70">
              Связь
            </p>
            <a href={`mailto:${CONTACT_EMAIL}`} className="mt-2 block text-sm font-medium text-highlight hover:underline">
              {CONTACT_EMAIL}
            </a>
          </div>
          <div className="md:max-w-md">
            <p className="text-xs font-semibold uppercase tracking-wider text-paragraph/70">
              О проекте
            </p>
            <p className="mt-2 text-sm leading-relaxed text-paragraph/90">
              {PROJECT_NOTE}
            </p>
          </div>
        </div>
        <p className="mt-10 text-center text-[11px] text-paragraph/50">
          © {new Date().getFullYear()} PlanningFlow
        </p>
      </footer>
    </div>);
};
