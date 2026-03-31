import client from './client';
import type { Metadata } from '../types';

export const getMetadata = () => client.get<Metadata>('/metadata');

