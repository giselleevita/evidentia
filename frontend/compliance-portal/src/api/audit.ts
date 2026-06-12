import { apiClient } from './client';

export interface AuditEvent {
  id: string;
  tenantId: string;
  actor: string;
  action: string;
  resourceType: string;
  resourceId: string;
  correlationId: string;
  timestamp: string;
  metadata: Record<string, unknown>;
}

export const auditApi = {
  getTrail: async (limit: number = 100): Promise<AuditEvent[]> => {
    const response = await apiClient.get<AuditEvent[]>(`/api/v1/audit/trail?limit=${limit}`);
    return response.data;
  },

  getByCorrelationId: async (correlationId: string): Promise<AuditEvent[]> => {
    const response = await apiClient.get<AuditEvent[]>(`/api/v1/audit/correlation/${correlationId}`);
    return response.data;
  },

  getByResource: async (resourceType: string, resourceId: string): Promise<AuditEvent[]> => {
    const response = await apiClient.get<AuditEvent[]>(`/api/v1/audit/resource/${resourceType}/${resourceId}`);
    return response.data;
  },
};
