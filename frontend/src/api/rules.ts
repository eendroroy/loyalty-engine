import client from './client';
import type { Rule, RuleAction } from '../types';

type RulePayload = Omit<Rule, 'id' | 'actions' | 'lastRunAt' | 'createdAt' | 'updatedAt'>;
type ActionPayload = Omit<RuleAction, 'id'>;

export const getRules    = () => client.get<Rule[]>('/rules');
export const getRule     = (id: number) => client.get<Rule>(`/rules/${id}`);
export const createRule  = (data: RulePayload) => client.post<Rule>('/rules', data);
export const updateRule  = (id: number, data: RulePayload) => client.put<Rule>(`/rules/${id}`, data);
export const deleteRule  = (id: number) => client.delete(`/rules/${id}`);

export const validateExpression = (expression: string) =>
  client.post<{ valid: boolean; error?: string }>('/rules/validate-expression', { expression });

export const getActions    = (ruleId: number) => client.get<RuleAction[]>(`/rules/${ruleId}/actions`);
export const createAction  = (ruleId: number, data: ActionPayload) =>
  client.post<RuleAction>(`/rules/${ruleId}/actions`, data);
export const updateAction  = (ruleId: number, aId: number, data: ActionPayload) =>
  client.put<RuleAction>(`/rules/${ruleId}/actions/${aId}`, data);
export const deleteAction  = (ruleId: number, aId: number) =>
  client.delete(`/rules/${ruleId}/actions/${aId}`);

