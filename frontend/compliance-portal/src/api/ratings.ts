import { apiClient } from './client';
import type { ApiResponse } from './client';

export interface Rating {
  id: string;
  tenantId: string;
  resourceType: string;
  resourceId: string;
  raterId: string;
  value: number;
  comment?: string;
  createdAt: string;
  updatedAt: string;
}

export interface RatingSummary {
  resourceType: string;
  resourceId: string;
  averageRating: number;
  totalRatings: number;
}

export interface UserStatistics {
  userId: string;
  totalRatingsGiven: number;
  averageRatingGiven: number;
  ratingDistribution: Record<number, number>;
}

export interface CreateRatingRequest {
  resourceType: string;
  resourceId: string;
  value: number;
  comment?: string;
}

export interface UpdateRatingRequest {
  value?: number;
  comment?: string;
}

export interface UserAccount {
  userId: string;
  email: string;
  name?: string | null;
  tenantId: string;
}

export const ratingsApi = {
  create: async (data: CreateRatingRequest): Promise<Rating> => {
    const response = await apiClient.post<ApiResponse<Rating>>('/ratings', data);
    return response.data.data!;
  },

  update: async (ratingId: string, data: UpdateRatingRequest): Promise<Rating> => {
    const response = await apiClient.put<ApiResponse<Rating>>(`/ratings/${ratingId}`, data);
    return response.data.data!;
  },

  delete: async (ratingId: string): Promise<void> => {
    await apiClient.delete(`/ratings/${ratingId}`);
  },

  get: async (ratingId: string): Promise<Rating> => {
    const response = await apiClient.get<ApiResponse<Rating>>(`/ratings/${ratingId}`);
    return response.data.data!;
  },

  getByResource: async (resourceType: string, resourceId: string): Promise<Rating[]> => {
    const response = await apiClient.get<ApiResponse<Rating[]>>(
      `/ratings/resource/${resourceType}/${resourceId}`
    );
    return response.data.data!;
  },

  getResourceSummary: async (resourceType: string, resourceId: string): Promise<RatingSummary> => {
    const response = await apiClient.get<ApiResponse<RatingSummary>>(
      `/ratings/resource/${resourceType}/${resourceId}/summary`
    );
    return response.data.data!;
  },

  getMyRatings: async (): Promise<Rating[]> => {
    const response = await apiClient.get<ApiResponse<Rating[]>>('/ratings/my-ratings');
    return response.data.data!;
  },

  getAccount: async (): Promise<UserAccount> => {
    const response = await apiClient.get<ApiResponse<UserAccount>>('/ratings/account/me');
    return response.data.data!;
  },

  getAccountStatistics: async (): Promise<UserStatistics> => {
    const response = await apiClient.get<ApiResponse<UserStatistics>>('/ratings/account/me/statistics');
    return response.data.data!;
  },

  getUserStatistics: async (raterId: string): Promise<UserStatistics> => {
    const response = await apiClient.get<ApiResponse<UserStatistics>>(`/ratings/user/${raterId}/statistics`);
    return response.data.data!;
  },
};
