import client from './client';
import type {
  DataSource,
  DataSourceFile,
  DataSourceField,
  DataSourceSchemaField,
  DataSourceWebhook,
  WebhookProperty,
  WebhookContentType,
  TableDataPage,
  FieldDataType,
} from '../types';

export type SchemaFieldPayload = { name: string; description?: string; dataType: FieldDataType };
export type FieldPayload    = Omit<DataSourceField, 'id'>;
export type FilePayload     = Omit<DataSourceFile, 'id' | 'createdAt' | 'updatedAt'> & {
  fields?: FieldPayload[];
};
export type WebhookPayload  = { enabled?: boolean; description?: string };  // only enabled + description; properties auto-synced

export type DSPayload = {
  name: string;
  description?: string;
  destinationTable?: string;
  fields?: SchemaFieldPayload[];
  files?: FilePayload[];
  webhook?: WebhookPayload;  // singular
};

// Data Sources
export const getDataSources = () => client.get<DataSource[]>('/data-sources');
export const getArchivedDataSources = () => client.get<DataSource[]>('/data-sources/archived');
export const getDataSourcesWithTables = () => client.get<DataSource[]>('/data-sources/with-tables');
export const getDataSource = (id: number) => client.get<DataSource>(`/data-sources/${id}`);
export const createDataSource = (data: DSPayload) => client.post<DataSource>('/data-sources', data);
export const updateDataSource = (id: number, data: DSPayload) =>
  client.put<DataSource>(`/data-sources/${id}`, data);
export const deleteDataSource = (id: number) => client.delete(`/data-sources/${id}`);

// Archive / purge lifecycle
export const archiveDataSource    = (id: number) => client.post(`/data-sources/${id}/archive`);
export const purgeArchivedSource  = (id: number) => client.delete(`/data-sources/${id}/purge`);
export const purgeDestinationData = (id: number) => client.delete(`/data-sources/${id}/destination-data`);
export const dropDestinationTable = (id: number) => client.delete(`/data-sources/${id}/destination-table`);

// Destination table data
export type TableQueryParams = {
  search?: string;
  page?: number;
  size?: number;
  sortBy?: string;
  sortDir?: 'asc' | 'desc';
};
export const getTableData = (dsId: number, params: TableQueryParams = {}) =>
  client.get<TableDataPage>(`/data-sources/${dsId}/table/data`, { params });

// Schema fields (destination-column schema)
export const getSchemaFields = (dsId: number) =>
  client.get<DataSourceSchemaField[]>(`/data-sources/${dsId}/schema-fields`);
export const createSchemaField = (dsId: number, data: SchemaFieldPayload) =>
  client.post<DataSourceSchemaField>(`/data-sources/${dsId}/schema-fields`, data);
export const updateSchemaField = (dsId: number, fId: number, data: SchemaFieldPayload) =>
  client.put<DataSourceSchemaField>(`/data-sources/${dsId}/schema-fields/${fId}`, data);
export const deleteSchemaField = (dsId: number, fId: number) =>
  client.delete(`/data-sources/${dsId}/schema-fields/${fId}`);

// Files
export const getFiles = (dsId: number) => client.get<DataSourceFile[]>(`/data-sources/${dsId}/files`);
export const createFile = (dsId: number, data: FilePayload) =>
  client.post<DataSourceFile>(`/data-sources/${dsId}/files`, data);
export const updateFile = (dsId: number, fId: number, data: FilePayload) =>
  client.put<DataSourceFile>(`/data-sources/${dsId}/files/${fId}`, data);
export const deleteFile = (dsId: number, fId: number) =>
  client.delete(`/data-sources/${dsId}/files/${fId}`);

// Webhooks
export const getWebhooks = (dsId: number) => client.get<DataSourceWebhook[]>(`/data-sources/${dsId}/webhooks`);
export const createWebhook = (dsId: number, data: WebhookPayload) =>
  client.post<DataSourceWebhook>(`/data-sources/${dsId}/webhooks`, data);
export const updateWebhook = (dsId: number, wId: number, data: WebhookPayload) =>
  client.put<DataSourceWebhook>(`/data-sources/${dsId}/webhooks/${wId}`, data);
export const deleteWebhook = (dsId: number, wId: number) =>
  client.delete(`/data-sources/${dsId}/webhooks/${wId}`);

// Fields (nested under files)
export const getFields = (dsId: number, fileId: number) =>
  client.get<DataSourceField[]>(`/data-sources/${dsId}/files/${fileId}/fields`);
export const createField = (dsId: number, fileId: number, data: FieldPayload) =>
  client.post<DataSourceField>(`/data-sources/${dsId}/files/${fileId}/fields`, data);
export const updateField = (dsId: number, fileId: number, fId: number, data: FieldPayload) =>
  client.put<DataSourceField>(`/data-sources/${dsId}/files/${fileId}/fields/${fId}`, data);
export const deleteField = (dsId: number, fileId: number, fId: number) =>
  client.delete(`/data-sources/${dsId}/files/${fileId}/fields/${fId}`);

// Webhook Properties (nested under webhooks)
export const getWebhookProperties = (dsId: number, wId: number) =>
  client.get<WebhookProperty[]>(`/data-sources/${dsId}/webhooks/${wId}/properties`);
export const createWebhookProperty = (dsId: number, wId: number, data: WebhookProperty) =>
  client.post<WebhookProperty>(`/data-sources/${dsId}/webhooks/${wId}/properties`, data);
export const updateWebhookProperty = (dsId: number, wId: number, pId: number,
                                       data: WebhookProperty) =>
  client.put<WebhookProperty>(`/data-sources/${dsId}/webhooks/${wId}/properties/${pId}`, data);
export const deleteWebhookProperty = (dsId: number, wId: number, pId: number) =>
  client.delete(`/data-sources/${dsId}/webhooks/${wId}/properties/${pId}`);

