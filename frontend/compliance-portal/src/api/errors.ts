import axios from 'axios';

export const getApiErrorMessage = (error: unknown, fallback: string): string => {
  if (axios.isAxiosError(error)) {
    const message = error.response?.data?.error?.message;
    if (typeof message === 'string' && message.length > 0) {
      return message;
    }
  }

  return error instanceof Error && error.message ? error.message : fallback;
};
