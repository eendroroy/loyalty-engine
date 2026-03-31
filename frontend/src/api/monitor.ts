import client from './client';
import type { FileWatcherMonitor, WebhookMonitor, FileProcessingLog, Page } from '../types';

export const getFileWatchers = () =>
  client.get<FileWatcherMonitor[]>('/monitors/file-watchers');

export const getWebhooks = () =>
  client.get<WebhookMonitor[]>('/monitors/webhooks');

export const getLogs = (page = 0, size = 50) =>
  client.get<Page<FileProcessingLog>>(`/monitors/logs?page=${page}&size=${size}`);

