import axios, { AxiosInstance } from 'axios';
import { getAccessToken, logout } from '../auth/auth';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const apiClient: AxiosInstance = axios.create({
  baseURL: API_BASE_URL,
  headers: {
    'Content-Type': 'application/json',
  },
});

// Request interceptor to add auth token
apiClient.interceptors.request.use(
  async (config) => {
    const token = await getAccessToken();
    if (token) {
      config.headers.Authorization = `Bearer ${token}`;
    }
    
    // Add correlation ID
    config.headers['X-Correlation-Id'] = crypto.randomUUID();
    
    return config;
  },
  (error) => Promise.reject(error)
);

// Response interceptor to unwrap ApiResponse
apiClient.interceptors.response.use(
  (response) => {
    // Backend returns ApiResponse<T>, extract data if present
    if (response.data && typeof response.data === 'object' && 'data' in response.data) {
      return response;
    }
    return response;
  },
  (error) => {
    // Handle error responses
    return Promise.reject(error);
  }
);

// Response interceptor for error handling and unwrapping ApiResponse
apiClient.interceptors.response.use(
  (response) => {
    // Backend returns ApiResponse<T> wrapper
    if (response.data && typeof response.data === 'object' && 'success' in response.data) {
      // Return the data directly for easier consumption
      return response;
    }
    return response;
  },
  (error) => {
    if (error.response?.status === 401) {
      // Handle unauthorized - logout and redirect to login
      logout(API_BASE_URL).catch(() => {
        window.location.href = '/login';
      });
    }
    return Promise.reject(error);
  }
);
