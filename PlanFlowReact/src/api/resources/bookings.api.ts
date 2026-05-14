import { type BookingId, type PageQuery, type PageResult, type ResourceBookingRescheduleRequest, type ResourceBookingResponseDto, type TaskId, } from '@/types';
import { http } from '../http';
import { ENDPOINTS } from '../endpoints';
export const bookingsApi = {
    forTask: (taskId: TaskId, query: PageQuery): Promise<PageResult<ResourceBookingResponseDto>> => http
        .get<PageResult<ResourceBookingResponseDto>>(ENDPOINTS.bookings.forTask(taskId), {
        params: query,
    })
        .then((r) => r.data),
    byId: (id: BookingId): Promise<ResourceBookingResponseDto> => http
        .get<ResourceBookingResponseDto>(ENDPOINTS.bookings.byId(id))
        .then((r) => r.data),
    reschedule: (id: BookingId, body: ResourceBookingRescheduleRequest): Promise<number> => http
        .put<number>(ENDPOINTS.bookings.schedule(id), body)
        .then((r) => r.data),
    confirm: (id: BookingId): Promise<number> => http
        .post<number>(ENDPOINTS.bookings.confirm(id))
        .then((r) => r.data),
    fail: (id: BookingId): Promise<number> => http
        .post<number>(ENDPOINTS.bookings.fail(id))
        .then((r) => r.data),
    cancel: (id: BookingId): Promise<number> => http
        .post<number>(ENDPOINTS.bookings.cancel(id))
        .then((r) => r.data),
};
