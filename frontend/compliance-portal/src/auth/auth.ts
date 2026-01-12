// Placeholder for Azure AD MSAL integration
export const getAccessToken = async (): Promise<string | null> => {
  // TODO: Implement MSAL token acquisition
  // For now, return null (will need proper Azure AD setup)
  return null;
};

/**
 * Simple logout function that:
 * 1. Calls backend logout endpoint to log the event (non-blocking)
 * 2. Clears all local storage (tokens, user data, etc.)
 * 3. Redirects to login page
 * 
 * This is designed to be fast and reliable - it will always redirect even if the backend call fails.
 */
export const logout = async (apiBaseUrl: string = '/api/v1'): Promise<void> => {
  // Get token from storage before clearing
  const token = localStorage.getItem('access_token') || sessionStorage.getItem('access_token');
  
  // Clear storage immediately (don't wait for backend)
  localStorage.clear();
  sessionStorage.clear();
  
  // Try to call backend logout (fire and forget - don't wait)
  if (token) {
    fetch(`${apiBaseUrl}/auth/logout`, {
      method: 'POST',
      headers: {
        'Authorization': `Bearer ${token}`,
        'Content-Type': 'application/json',
      },
    }).catch(() => {
      // Ignore errors - we're logging out anyway
    });
  }
  
  // Redirect immediately (don't wait for backend response)
  window.location.href = '/login';
};
