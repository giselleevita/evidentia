import { useQuery } from '@tanstack/react-query';
import { auditApi } from '../api/audit';

export const useAuditTrail = (limit: number = 100) => {
  return useQuery({
    queryKey: ['audit', 'trail', limit],
    queryFn: () => auditApi.getTrail(limit),
  });
};

export const useAuditByResource = (resourceType: string, resourceId: string) => {
  return useQuery({
    queryKey: ['audit', 'resource', resourceType, resourceId],
    queryFn: () => auditApi.getByResource(resourceType, resourceId),
    enabled: !!resourceType && !!resourceId,
  });
};

export const useAuditByCorrelationId = (correlationId: string) => {
  return useQuery({
    queryKey: ['audit', 'correlation', correlationId],
    queryFn: () => auditApi.getByCorrelationId(correlationId),
    enabled: !!correlationId,
  });
};
