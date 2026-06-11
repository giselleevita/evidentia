import { apiClient } from './client';

export interface Incident {
  id: string;
  tenantId: string;
  title: string;
  description: string;
  severity: string;
  status: string;
  createdAt: string;
  updatedAt: string;
  createdBy: string;
  resolvedAt?: string;
  resolvedBy?: string;
  reviewedAt?: string;
  reviewedBy?: string;
  reviewNotes?: string;
  escalationNote?: string;
}

export interface CreateIncidentRequest {
  title: string;
  description: string;
  severity: string;
}

export interface EscalateIncidentRequest {
  escalationNote: string;
}

export interface ResolveIncidentRequest {
  resolutionNote: string;
}

export interface ReviewIncidentRequest {
  reviewNotes: string;
}

export const incidentsApi = {
  list: async (status?: string): Promise<Incident[]> => {
    const params = status ? `?status=${status}` : '';
    const response = await apiClient.get<Incident[]>(`/api/v1/incidents${params}`);
    return response.data;
  },

  get: async (id: string): Promise<Incident> => {
    const response = await apiClient.get(`/api/v1/incidents/${id}`);
    // Incident service returns data directly, not wrapped in ApiResponse
    return response.data;
  },

  create: async (data: CreateIncidentRequest): Promise<Incident> => {
    const response = await apiClient.post('/api/v1/incidents', data);
    return response.data;
  },

  escalate: async (id: string, data: EscalateIncidentRequest): Promise<Incident> => {
    const response = await apiClient.post(`/api/v1/incidents/${id}/escalate`, data);
    return response.data;
  },

  resolve: async (id: string, data: ResolveIncidentRequest): Promise<Incident> => {
    const response = await apiClient.post(`/api/v1/incidents/${id}/resolve`, data);
    return response.data;
  },

  review: async (id: string, data: ReviewIncidentRequest): Promise<Incident> => {
    const response = await apiClient.post(`/api/v1/incidents/${id}/review`, data);
    return response.data;
  },

  listBySeverity: async (severity: string): Promise<Incident[]> => {
    const response = await apiClient.get<Incident[]>(`/api/v1/incidents/severity/${severity}`);
    return response.data;
  },
};
