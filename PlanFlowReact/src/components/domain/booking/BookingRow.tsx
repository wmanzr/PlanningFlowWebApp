import { type ReactNode } from 'react';
import Chip from '@mui/material/Chip';
import Typography from '@mui/material/Typography';
import { Button, Card, formatDateTime } from '@/components/ui';
import { BookingStatus, ResourceType, ResourceSource, type ResourceBookingResponseDto, } from '@/types';
import { BookingStatusBadge } from './BookingStatusBadge';
const RESOURCE_TYPE_LABEL: Record<ResourceType, string> = {
    [ResourceType.EQUIPMENT]: 'Оборудование',
    [ResourceType.TRANSPORT]: 'Транспорт',
    [ResourceType.MATERIAL]: 'Материал',
};
const RESOURCE_SOURCE_LABEL: Record<ResourceSource, string> = {
    [ResourceSource.INTERNAL]: 'Внутренний склад',
    [ResourceSource.EXTERNAL]: 'Внешний поставщик',
    [ResourceSource.UNKNOWN]: 'Источник неизвестен',
};
function resourceSourceChipLabel(source: ResourceSource | string): string {
    if (source === ResourceSource.INTERNAL || source === ResourceSource.EXTERNAL || source === ResourceSource.UNKNOWN) {
        return RESOURCE_SOURCE_LABEL[source];
    }
    return source;
}
export interface BookingRowProps {
    booking: ResourceBookingResponseDto;
    actions?: ReactNode;
    rowHoverCancel?: {
        onCancel: () => void;
        loading?: boolean;
    };
}
export const BookingRow = ({ booking, actions, rowHoverCancel }: BookingRowProps) => {
    const showRowHoverCancel = rowHoverCancel != null && booking.status !== BookingStatus.CANCELLED;
    return (<Card className={showRowHoverCancel ? 'group relative' : undefined}>
      <div className="relative flex items-start justify-between gap-3">
        {showRowHoverCancel ? (<div className="pointer-events-none absolute right-3 top-1/2 z-10 -translate-y-1/2 opacity-0 shadow-sm transition-opacity duration-150 group-hover:pointer-events-auto group-hover:opacity-100">
            <Button type="button" size="sm" variant="danger" loading={rowHoverCancel.loading} className="pointer-events-auto" onClick={(e) => {
                e.stopPropagation();
                rowHoverCancel.onCancel();
            }}>
              Отменить
            </Button>
          </div>) : null}
        <div className={`min-w-0 flex-1 ${showRowHoverCancel ? 'pr-[7.25rem]' : ''}`}>
          <div className="flex flex-wrap items-center gap-2">
            <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 600 }}>
              {booking.resourceName ?? 'Ресурс'}
            </Typography>
            <BookingStatusBadge status={booking.status}/>
            {booking.resourceType ? (<Typography variant="caption" color="text.secondary">
                {RESOURCE_TYPE_LABEL[booking.resourceType]}
              </Typography>) : null}
            {booking.resourceSource ? (<Chip label={resourceSourceChipLabel(booking.resourceSource)} size="small" variant="outlined"/>) : null}
          </div>
          <Typography variant="caption" color="text.secondary" className="mt-2" sx={{ display: 'block' }}>
            {formatDateTime(booking.reservedFrom)} → {formatDateTime(booking.reservedTo)}
          </Typography>
        </div>
        {!showRowHoverCancel && actions ? (<div className="flex shrink-0 flex-wrap gap-2">{actions}</div>) : null}
      </div>
    </Card>);
};
