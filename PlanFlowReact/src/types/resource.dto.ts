import { type ResourceType } from './enums';
import { type ResourceId } from './common';
export interface InternalResourceResponseDto {
    id: ResourceId;
    name: string;
    type: ResourceType;
    inventoryNumber: string;
    operational: boolean;
}
export interface ExternalResourceResponseDto {
    id: ResourceId;
    name: string;
    type: ResourceType;
    externalApiId: string;
    operational: boolean;
}
export interface InternalResourceCreateRequest {
    name: string;
    type: ResourceType;
    inventoryNumber: string;
}
export interface InternalResourceUpdateRequest {
    name?: string;
    type?: ResourceType;
    inventoryNumber?: string;
}
