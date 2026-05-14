import { useNavigate } from 'react-router-dom';
import Paper from '@mui/material/Paper';
import Typography from '@mui/material/Typography';
import Box from '@mui/material/Box';
import { Button, PageLayout } from '@/components/ui';
import { PATHS } from '../paths';
export const ForbiddenPage = () => {
    const navigate = useNavigate();
    const handleBack = (): void => {
        if (typeof window !== 'undefined' && window.history.length > 1) {
            navigate(-1);
        }
        else {
            navigate(PATHS.home, { replace: true });
        }
    };
    return (<PageLayout title="Доступ ограничен">
      <Paper variant="outlined" sx={{
            mx: 'auto',
            maxWidth: 512,
            display: 'flex',
            flexDirection: 'column',
            alignItems: 'center',
            px: 4,
            py: 7,
            textAlign: 'center',
        }}>
        <Typography variant="h2" sx={{ fontWeight: 700, fontFamily: 'monospace', mb: 1 }} color="primary" aria-hidden>
          403
        </Typography>
        <Typography variant="h6" sx={{ fontWeight: 600 }} color="text.primary">
          Содержимое этой страницы вам недоступно
        </Typography>
        <Typography variant="body2" color="text.secondary" sx={{ mt: 1.5, maxWidth: 360 }}>
          У вашей учетной записи нет прав на просмотр или выполнение этого действия. Если вы
          считаете, что это ошибка, обратитесь к организатору мероприятия или администратору.
        </Typography>
        <Box sx={{ mt: 4, display: 'flex', flexWrap: 'wrap', alignItems: 'center', justifyContent: 'center', gap: 1.5 }}>
          <Button type="button" onClick={handleBack}>
            Вернуться назад
          </Button>
          <Button type="button" variant="secondary" onClick={() => navigate(PATHS.home)}>
            На главную
          </Button>
        </Box>
      </Paper>
    </PageLayout>);
};
