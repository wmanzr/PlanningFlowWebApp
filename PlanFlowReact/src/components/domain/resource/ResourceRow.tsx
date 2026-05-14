import { type ReactNode } from 'react';
import Typography from '@mui/material/Typography';
import { Badge, Card } from '@/components/ui';
import { ResourceType, type InternalResourceResponseDto, } from '@/types';
export const RESOURCE_TYPE_LABEL: Record<ResourceType, string> = {
    [ResourceType.EQUIPMENT]: 'Оборудование',
    [ResourceType.TRANSPORT]: 'Транспорт',
    [ResourceType.MATERIAL]: 'Материал',
};
export interface ResourceRowProps {
    resource: InternalResourceResponseDto;
    actions?: ReactNode;
}
export const ResourceRow = ({ resource, actions }: ResourceRowProps) => (<Card className="group transition-colors duration-200 hover:bg-secondary/30">
    <div className="flex items-start justify-between gap-3">
      <div className="flex-1">
        <div className="flex flex-wrap items-center gap-2">
          <Typography variant="subtitle1" component="h3" sx={{ fontWeight: 600 }}>
            {resource.name}
          </Typography>
          <Badge tone={resource.operational ? 'success' : 'danger'}>
            {resource.operational ? 'Исправен' : 'Неисправен'}
          </Badge>
          <Badge tone="info">{RESOURCE_TYPE_LABEL[resource.type]}</Badge>
        </div>
        <Typography variant="caption" color="text.secondary" className="mt-2" sx={{ display: 'block' }}>
          Инвентарный №: <span className="font-mono">{resource.inventoryNumber}</span>
        </Typography>
      </div>
      {actions ? (<div className="pointer-events-none flex min-w-0 shrink-0 translate-x-1 flex-col items-end gap-2 opacity-0 transition-all duration-200 ease-out group-hover:pointer-events-auto group-hover:translate-x-0 group-hover:opacity-100">
          {actions}
        </div>) : null}
    </div>
  </Card>);
