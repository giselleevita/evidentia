import { apiClient } from './client';
import { Evidence, CreateEvidenceRequest, UpdateEvidenceStatusRequest, EvidenceSchema, ApiResponseSchema } from '../types/evidence';
import { z } from 'zod';

const EvidenceResponseSchema = ApiResponseSchema(EvidenceSchema);
const EvidenceListResponseSchema = ApiResponseSchema(z.array(EvidenceSchema));

export const evidenceApi = {
  list: async (): Promise<Evidence[]> => {
    const response = await apiClient.get('/evidence');
    const parsed = EvidenceListResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to fetch evidence');
    }
    return parsed.data;
  },

  get: async (id: string): Promise<Evidence> => {
    const response = await apiClient.get(`/evidence/${id}`);
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to fetch evidence');
    }
    return parsed.data;
  },

  create: async (request: CreateEvidenceRequest): Promise<Evidence> => {
    const response = await apiClient.post('/evidence', request);
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to create evidence');
    }
    return parsed.data;
  },

  update: async (id: string, request: { title?: string; description?: string; type?: string; sourceSystem?: string; references?: Record<string, string> }): Promise<Evidence> => {
    const response = await apiClient.put(`/evidence/${id}`, request);
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to update evidence');
    }
    return parsed.data;
  },

  submitForReview: async (id: string, note?: string): Promise<Evidence> => {
    const response = await apiClient.post(`/evidence/${id}/submit`, { note });
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to submit evidence');
    }
    return parsed.data;
  },

  approve: async (id: string, note?: string): Promise<Evidence> => {
    const response = await apiClient.post(`/evidence/${id}/approve`, { note });
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to approve evidence');
    }
    return parsed.data;
  },

  reject: async (id: string, reason: string): Promise<Evidence> => {
    const response = await apiClient.post(`/evidence/${id}/reject`, { reason });
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to reject evidence');
    }
    return parsed.data;
  },

  lock: async (id: string): Promise<Evidence> => {
    const response = await apiClient.post(`/evidence/${id}/lock`);
    const parsed = EvidenceResponseSchema.parse(response.data);
    if (!parsed.success || !parsed.data) {
      throw new Error(parsed.error?.message || 'Failed to lock evidence');
    }
    return parsed.data;
  },
};
