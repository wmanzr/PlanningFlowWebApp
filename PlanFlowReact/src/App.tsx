import { useMemo } from 'react';
import { createBrowserRouter, RouterProvider } from 'react-router-dom';
import { ErrorBoundary } from '@/components/ui';
import { AppBootstrap } from '@/pages/_app/AppBootstrap';
import { AppThemeBridge } from '@/pages/_app/AppThemeBridge';
import { AppToastsBridge } from '@/pages/_app/AppToastsBridge';
import { buildRoutes } from '@/pages/routes';
export const App = () => {
    const router = useMemo(() => createBrowserRouter(buildRoutes()), []);
    return (<ErrorBoundary>
      <AppThemeBridge>
        <AppBootstrap>
          <RouterProvider router={router}/>
        </AppBootstrap>
        <AppToastsBridge />
      </AppThemeBridge>
    </ErrorBoundary>);
};
export default App;
