import { Link } from 'react-router-dom';
import { Button, EmptyState, PageLayout } from '@/components/ui';
import { PATHS } from '../paths';
export const NotFoundPage = () => (<PageLayout title="Страница не найдена">
    <EmptyState title="404" description="Похоже, такого маршрута нет." action={<Link to={PATHS.home}>
          <Button>На главную</Button>
        </Link>}/>
  </PageLayout>);
