import { AccountInfo, PublicClientApplication } from '@azure/msal-browser';

const clientId = import.meta.env.VITE_AZURE_CLIENT_ID;
const tenantId = import.meta.env.VITE_AZURE_TENANT_ID;
const scopes = (import.meta.env.VITE_AZURE_SCOPES || '').split(',').map((scope) => scope.trim()).filter(Boolean);
const demoMode = import.meta.env.VITE_DEMO_MODE === 'true';

let clientPromise: Promise<PublicClientApplication> | null = null;

const getClient = async (): Promise<PublicClientApplication | null> => {
  if (!isAuthConfigured()) {
    return null;
  }

  if (!clientPromise) {
    clientPromise = (async () => {
      const client = new PublicClientApplication({
        auth: {
          clientId: clientId!,
          authority: `https://login.microsoftonline.com/${tenantId!}`,
          redirectUri: window.location.origin,
          postLogoutRedirectUri: window.location.origin,
        },
        cache: {
          cacheLocation: 'sessionStorage',
          storeAuthStateInCookie: false,
        },
      });
      await client.initialize();
      const redirectResult = await client.handleRedirectPromise();
      const account = redirectResult?.account ?? client.getAllAccounts()[0];
      if (account) {
        client.setActiveAccount(account);
      }
      return client;
    })();
  }

  return clientPromise;
};

const activeAccount = (client: PublicClientApplication): AccountInfo | null =>
  client.getActiveAccount() ?? client.getAllAccounts()[0] ?? null;

export const isDemoMode = (): boolean => demoMode;

export const isAuthConfigured = (): boolean =>
  demoMode || Boolean(clientId && tenantId && scopes.length > 0);

export const hasAuthenticatedAccount = async (): Promise<boolean> => {
  if (demoMode) {
    return true;
  }
  const client = await getClient();
  return client ? activeAccount(client) !== null : false;
};

export const login = async (): Promise<void> => {
  const client = await getClient();
  if (!client) {
    throw new Error('Azure Entra ID frontend configuration is missing');
  }
  await client.loginRedirect({ scopes });
};

export const getAccessToken = async (): Promise<string | null> => {
  if (demoMode) {
    return null;
  }
  const client = await getClient();
  const account = client ? activeAccount(client) : null;
  if (!client || !account) {
    return null;
  }

  const result = await client.acquireTokenSilent({ account, scopes });
  return result.accessToken;
};

/**
 * Simple logout function that:
 * 1. Calls backend logout endpoint to log the event (non-blocking)
 * 2. Ends the MSAL session and clears its scoped session cache
 * 3. Redirects through the configured identity-provider logout flow
 * 
 * This is designed to be fast and reliable - it will always redirect even if the backend call fails.
 */
export const logout = async (apiBaseUrl: string = '/api/v1'): Promise<void> => {
  const token = await getAccessToken().catch(() => null);
  
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
  
  if (demoMode) {
    window.location.href = '/';
    return;
  }

  const client = await getClient();
  if (client) {
    await client.logoutRedirect({ account: activeAccount(client) ?? undefined });
  }
};
