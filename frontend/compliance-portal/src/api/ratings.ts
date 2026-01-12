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
  average: number;
  count: number;
  distribution: Record<number, number>;
}

export interface UserStatistics {
  totalRatings: number;
  averageGiven: number;
  averageReceived: number;
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
  id: string;
  email: string;
  name?: string;
}

export const ratingsApi = {
  create: async (data: CreateRatingRequest): Promise<Rating> => {
    const response = await apiClient.post<ApiResponse<Rating>>('/api/v1/ratings', data);
    return response.data.data!;
  },

  update: async (ratingId: string, data: UpdateRatingRequest): Promise<Rating> => {
    const response = await apiClient.put<ApiResponse<Rating>>(`/api/v1/ratings/${ratingId}`, data);
    return response.data.data!;
  },

  delete: async (ratingId: string): Promise<void> => {
    await apiClient.delete(`/api/v1/ratings/${ratingId}`);
  },

  get: async (ratingId: string): Promise<Rating> => {
    const response = await apiClient.get<ApiResponse<Rating>>(`/api/v1/ratings/${ratingId}`);
    return response.data.data!;
  },

  getByResource: async (resourceType: string, resourceId: string): Promise<Rating[]> => {
    const response = await apiClient.get<ApiResponse<Rating[]>>(
      `/api/v1/ratings/resource/${resourceType}/${resourceId}`
    );
    return response.data.data!;
  },

  getResourceSummary: async (resourceType: string, resourceId: string): Promise<RatingSummary> => {
    const response = await apiClient.get<ApiResponse<RatingSummary>>(
      `/api/v1/ratings/resource/${resourceType}/${resourceId}/summary`
    );
    return response.data.data!;
  },

  getMyRatings: async (): Promise<Rating[]> => {
    const response = await apiClient.get<ApiResponse<Rating[]>>('/api/v1/ratings/my-ratings');
    return response.data.data!;
  },

  getAccount: async (): Promise<UserAccount> => {
    const response = await apiClient.get<ApiResponse<UserAccount>>('/api/v1/ratings/account/me');
    return response.data.data!;
  },

  getAccountStatistics: async (): Promise<UserStatistics> => {
    const response = await apiClient.get<ApiResponse<UserStatistics>>('/api/v1/ratings/account/me/statistics');
    return response.data.data!;
  },

  getUserStatistics: async (raterId: string): Promise<UserStatistics> => {
    const response = await apiClient.get<ApiResponse<UserStatistics>>(`/api/v1/ratings/user/${raterId}/statistics`);
    return response.data.data!;
  },
};
