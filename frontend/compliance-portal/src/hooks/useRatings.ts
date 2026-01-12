import { useQuery, useMutation, useQueryClient } from '@tanstack/react-query';
import { ratingsApi, CreateRatingRequest, UpdateRatingRequest } from '../api/ratings';

export const useRatings = (resourceType: string, resourceId: string) => {
  return useQuery({
    queryKey: ['ratings', resourceType, resourceId],
    queryFn: () => ratingsApi.getByResource(resourceType, resourceId),
    enabled: !!resourceType && !!resourceId,
  });
};

export const useRatingSummary = (resourceType: string, resourceId: string) => {
  return useQuery({
    queryKey: ['ratings', 'summary', resourceType, resourceId],
    queryFn: () => ratingsApi.getResourceSummary(resourceType, resourceId),
    enabled: !!resourceType && !!resourceId,
  });
};

export const useMyRatings = () => {
  return useQuery({
    queryKey: ['ratings', 'my-ratings'],
    queryFn: () => ratingsApi.getMyRatings(),
  });
};

export const useAccountStatistics = () => {
  return useQuery({
    queryKey: ['ratings', 'account', 'statistics'],
    queryFn: () => ratingsApi.getAccountStatistics(),
  });
};

export const useCreateRating = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (request: CreateRatingRequest) => ratingsApi.create(request),
    onSuccess: (_, variables) => {
      queryClient.invalidateQueries({ queryKey: ['ratings', variables.resourceType, variables.resourceId] });
      queryClient.invalidateQueries({ queryKey: ['ratings', 'summary'] });
      queryClient.invalidateQueries({ queryKey: ['ratings', 'my-ratings'] });
    },
  });
};

export const useUpdateRating = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: ({ ratingId, data }: { ratingId: string; data: UpdateRatingRequest }) =>
      ratingsApi.update(ratingId, data),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ratings'] });
    },
  });
};

export const useDeleteRating = () => {
  const queryClient = useQueryClient();
  
  return useMutation({
    mutationFn: (ratingId: string) => ratingsApi.delete(ratingId),
    onSuccess: () => {
      queryClient.invalidateQueries({ queryKey: ['ratings'] });
    },
  });
};
