import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { evidenceApi } from '../api/evidence';
import { CreateEvidenceRequest } from '../types/evidence';

export const useEvidenceList = () => {
  return useQuery({
    queryKey: ['evidence', 'list'],
    queryFn: () => evidenceApi.list(),
  });
};

export const useEvidence = (id: string) => {
  return useQuery({
    queryKey: ['evidence', id],
    queryFn: () => evidenceApi.get(id),
    enabled: !!id,
  });
};

export const useCreateEvidence = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (request: CreateEvidenceRequest) => evidenceApi.create(request),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['evidence', 'list'] });
    },
  });
};

export const useSubmitEvidence = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, note }: { id: string; note?: string }) =>
      evidenceApi.submitForReview(id, note),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['evidence', 'list'] });
      queryClient.invalidateQueries({ queryKey: ['evidence', variables.id] });
    },
  });
};

export const useApproveEvidence = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, note }: { id: string; note?: string }) =>
      evidenceApi.approve(id, note),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['evidence', 'list'] });
      queryClient.invalidateQueries({ queryKey: ['evidence', variables.id] });
    },
  });
};

export const useRejectEvidence = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ id, reason }: { id: string; reason: string }) =>
      evidenceApi.reject(id, reason),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['evidence', 'list'] });
      queryClient.invalidateQueries({ queryKey: ['evidence', variables.id] });
    },
  });
};

export const useLockEvidence = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (id: string) => evidenceApi.lock(id),
    onSuccess: (_, id) => {
      queryClient.invalidateQueries({ queryKey: ['evidence', 'list'] });
      queryClient.invalidateQueries({ queryKey: ['evidence', id] });
    },
  });
};
