import { Outlet } from 'react-router-dom';
import { AppForbiddenBridge } from '../_app/AppForbiddenBridge';
import { ShellLayout } from './ShellLayout';
export const AuthenticatedLayout = () => (<>
    <AppForbiddenBridge />
    <ShellLayout>
      <Outlet />
    </ShellLayout>
  </>);
