import { EmptyState, PageLayout } from '@/components/ui';
export interface ComingSoonPageProps {
    title: string;
    description?: string;
}
export const ComingSoonPage = ({ title, description }: ComingSoonPageProps) => (<PageLayout title={title}>
    <EmptyState title="Раздел в разработке" description={description ?? 'Этот раздел будет доступен в следующих шагах разработки.'}/>
  </PageLayout>);
