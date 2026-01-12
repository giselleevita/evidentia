import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { incidentsApi, CreateIncidentRequest, EscalateIncidentRequest, ResolveIncidentRequest, ReviewIncidentRequest } from '../api/incidents';

export const useIncidents = (status?: string) => {
  return useQuery({
    queryKey: ['incidents', status || 'all'],
    queryFn: () => incidentsApi.list(status),
  });
};

export const useIncident = (id: string) => {
  return useQuery({
    queryKey: ['incidents', id],
    queryFn: () => incidentsApi.get(id),
    enabled: !!id,
  });
};

export const useCreateIncident = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (request: CreateIncidentRequest) => incidentsApi.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
    },
  });
};

export const useEscalateIncident = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: EscalateIncidentRequest }) =>
      incidentsApi.escalate(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
      queryClient.invalidateQueries({ queryKey: ['incidents', variables.id] });
    },
  });
};

export const useResolveIncident = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ResolveIncidentRequest }) =>
      incidentsApi.resolve(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
      queryClient.invalidateQueries({ queryKey: ['incidents', variables.id] });
    },
  });
};

export const useReviewIncident = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, data }: { id: string; data: ReviewIncidentRequest }) =>
      incidentsApi.review(id, data),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['incidents'] });
      queryClient.invalidateQueries({ queryKey: ['incidents', variables.id] });
    },
  });
};
