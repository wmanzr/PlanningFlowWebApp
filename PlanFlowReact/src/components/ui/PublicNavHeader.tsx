import { Link } from 'react-router-dom';
import IconButton from '@mui/material/IconButton';
import DarkModeOutlinedIcon from '@mui/icons-material/DarkModeOutlined';
import LightModeOutlinedIcon from '@mui/icons-material/LightModeOutlined';
import { useAppDispatch, useAppSelector } from '@/store';
import { uiActions } from '@/store/slices/ui/uiSlice';
import { selectTheme } from '@/store/slices/ui/selectors';
import { PATHS } from '@/pages/paths';

/** Шапка для публичных страниц: логотип на лендинг и переключение темы. */
export function PublicNavHeader() {
    const dispatch = useAppDispatch();
    const mode = useAppSelector(selectTheme);
    return (<header className="relative z-10 flex w-full shrink-0 items-center justify-between px-5 pt-4 md:px-10">
      <Link to={PATHS.landing} className="text-sm font-semibold tracking-tight text-headline no-underline transition-opacity hover:opacity-80 md:text-base">
        PlanningFlow
      </Link>
      <IconButton onClick={() => dispatch(uiActions.toggleTheme())} aria-label="Переключить тему" size="small" sx={{
            border: `1px solid ${mode === 'dark' ? 'rgba(148,161,178,0.35)' : 'rgba(43,44,52,0.12)'}`,
            backgroundColor: mode === 'dark' ? 'rgba(31,33,40,0.6)' : 'rgba(255,255,254,0.7)',
            backdropFilter: 'blur(10px)',
        }}>
        {mode === 'dark' ? <LightModeOutlinedIcon fontSize="small"/> : <DarkModeOutlinedIcon fontSize="small"/>}
      </IconButton>
    </header>);
}
