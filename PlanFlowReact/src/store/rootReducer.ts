import { combineReducers } from '@reduxjs/toolkit';
import { authReducer } from './slices/auth/authSlice';
import { uiReducer } from './slices/ui/uiSlice';
import { toastsReducer } from './slices/toasts/toastsSlice';
import { eventsReducer } from './slices/events/eventsSlice';
import { tasksReducer } from './slices/tasks/tasksSlice';
import { matchingReducer } from './slices/matching/matchingSlice';
import { bookingsReducer } from './slices/bookings/bookingsSlice';
import { resourcesReducer } from './slices/resources/resourcesSlice';
import { usersReducer } from './slices/users/usersSlice';
import { skillsReducer } from './slices/skills/skillsSlice';
import { incidentsReducer } from './slices/incidents/incidentsSlice';
import { notificationsReducer } from './slices/notifications/notificationsSlice';
export const rootReducer = combineReducers({
    auth: authReducer,
    ui: uiReducer,
    toasts: toastsReducer,
    notifications: notificationsReducer,
    events: eventsReducer,
    tasks: tasksReducer,
    matching: matchingReducer,
    bookings: bookingsReducer,
    resources: resourcesReducer,
    users: usersReducer,
    skills: skillsReducer,
    incidents: incidentsReducer,
});
