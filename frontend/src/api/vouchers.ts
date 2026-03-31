import client from './client';
import type { Voucher, VoucherInstance } from '../types';

type VoucherPayload = Omit<Voucher, 'id' | 'archived' | 'instanceCount' | 'createdAt' | 'updatedAt'>;

export const getVouchers         = () => client.get<Voucher[]>('/vouchers');
export const getArchivedVouchers = () => client.get<Voucher[]>('/vouchers/archived');
export const getVoucher          = (id: number) => client.get<Voucher>(`/vouchers/${id}`);
export const createVoucher       = (data: VoucherPayload) => client.post<Voucher>('/vouchers', data);
export const updateVoucher       = (id: number, data: VoucherPayload) =>
  client.put<Voucher>(`/vouchers/${id}`, data);
export const archiveVoucher      = (id: number) => client.post(`/vouchers/${id}/archive`);
export const purgeVoucher        = (id: number) => client.delete(`/vouchers/${id}/purge`);

export const getVoucherInstances = (id: number) =>
  client.get<VoucherInstance[]>(`/vouchers/${id}/instances`);
export const awardVoucher        = (id: number) =>
  client.post<VoucherInstance>(`/vouchers/${id}/award`);

