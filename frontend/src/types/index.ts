export type FieldDataType = 'STRING' | 'INTEGER' | 'DECIMAL' | 'DATE' | 'BOOLEAN';
export type RuleStatus = 'DRAFT' | 'ACTIVE' | 'INACTIVE';
export type RewardType = 'POINT' | 'VOUCHER';
export type FileProcessingStatus = 'IN_PROGRESS' | 'COMPLETED' | 'FAILED';
/** Future: backend enum not yet added to DataSource entity. */
export type DataSourceType = 'FILE' | 'WEBHOOK';
/** FILE_WATCHER / INSTANT exist in backend; SCHEDULED is future work. */
export type TriggerType = 'FILE_WATCHER' | 'INSTANT';
/** Content type for inbound webhook payloads. */
export type WebhookContentType = 'APPLICATION_JSON';

// ── Imported table data ───────────────────────────────────────────────────────

export interface TableColumnMeta {
  alias: string;
  fieldName: string;
  dataType: FieldDataType;
  description?: string;
}

export interface TableDataPage {
  tableName: string;
  columns: TableColumnMeta[];
  rows: Record<string, unknown>[];
  totalElements: number;
  totalPages: number;
  page: number;
  size: number;
}

// ── Metadata (field-alias registry for rule autocomplete) ─────────────────────

export interface FieldMetadata {
  alias: string;
  fieldName: string;
  dataType: FieldDataType;
  columnNumber?: number;
  description?: string;
}

export interface DataSourceMetadata {
  id: number;
  name: string;
  fields: FieldMetadata[];
}

export interface Metadata {
  dataSources: DataSourceMetadata[];
  totalFields: number;
}

// ── Domain entities ───────────────────────────────────────────────────────────

export interface DataSourceSchemaField {
  id?: number;
  name: string;
  description?: string;
  dataType: FieldDataType;
  createdAt?: string;
  updatedAt?: string;
}

export interface DataSourceField {
  id?: number;
  fieldName: string;
  fieldAlias: string;
  columnNumber?: number;
  dataType: FieldDataType;
  description?: string;
}

export interface DataSourceFile {
  id?: number;
  filePath: string;
  description?: string;
  fieldSeparator?: string;
  quoteCharacter?: string;
  lineSeparator?: string;
  skipFirstNLines?: number;
  fields?: DataSourceField[];
  createdAt?: string;
  updatedAt?: string;
}

export interface DataSourceWebhook {
  id?: number;
  endpoint: string;          // auto-generated: /weeb-hook/{dataSourceName}
  enabled: boolean;          // true = active, false = disabled
  description?: string;
  properties?: DataSourceSchemaField[];  // mirrors schema fields (read-only)
  createdAt?: string;
  updatedAt?: string;
}

export interface WebhookProperty {
  id?: number;
  name: string;
  fieldAlias: string;
  dataType: FieldDataType;
  format?: string;
  description?: string;
}

export interface DataSource {
  id?: number;
  name: string;
  description?: string;
  destinationTable?: string;
  fields?: DataSourceSchemaField[];
  files?: DataSourceFile[];
  webhook?: DataSourceWebhook;  // singular: one webhook per data source
  archived?: boolean;
  createdAt?: string;
  updatedAt?: string;
}

export interface RuleAction {
  id?: number;
  rewardType: RewardType;
  rewardAmount: string;
  description?: string;
}

export interface Rule {
  id?: number;
  name: string;
  description?: string;
  ruleExpression: string;
  status: RuleStatus;
  priority: number;
  activeFrom?: string;
  activeTo?: string;
  frequency?: string;
  lastRunAt?: string;
  actions?: RuleAction[];
  createdAt?: string;
  updatedAt?: string;
}

// ── Monitoring ────────────────────────────────────────────────────────────────

export interface FileProcessingLogSummary {
  id: number;
  status: FileProcessingStatus;
  rowsIngested?: number;
  rowsSkipped?: number;
  errors?: number;
  instanceId?: string;
  startedAt?: string;
  completedAt?: string;
}

export interface FileWatcherMonitor {
  fileId: number;
  dataSourceId: number;
  sourceName: string;
  filePath: string;
  description?: string;
  watching: boolean;
  lastLog?: FileProcessingLogSummary;
  totalCompleted: number;
  totalFailed: number;
}

export interface WebhookMonitor {
  webhookId: number;
  dataSourceId: number;
  sourceName: string;
  endpoint: string;
  description?: string;
  createdAt?: string;
}

export interface FileProcessingLog {
  id: number;
  dataSourceFileId: number;
  sourceName: string;
  filePath: string;
  fileName?: string;
  fileSizeBytes?: number;
  lastModifiedMillis?: number;
  status: FileProcessingStatus;
  rowsIngested?: number;
  rowsSkipped?: number;
  errors?: number;
  instanceId?: string;
  startedAt?: string;
  completedAt?: string;
}

export interface Page<T> {
  content: T[];
  totalElements: number;
  totalPages: number;
  number: number;
  size: number;
}
