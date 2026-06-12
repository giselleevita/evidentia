import { ReactNode, useEffect, useState } from 'react';
import { hasAuthenticatedAccount, isAuthConfigured, isDemoMode, login } from './auth';

interface AuthBoundaryProps {
  children: ReactNode;
}

export const AuthBoundary = ({ children }: AuthBoundaryProps) => {
  const [authenticated, setAuthenticated] = useState(isDemoMode());
  const [loading, setLoading] = useState(!isDemoMode());

  useEffect(() => {
    if (isDemoMode()) {
      return;
    }

    hasAuthenticatedAccount()
      .then(setAuthenticated)
      .finally(() => setLoading(false));
  }, []);

  if (loading) {
    return <div className="auth-status">Checking authentication...</div>;
  }

  if (!isAuthConfigured()) {
    return (
      <div className="auth-status">
        <h1>Authentication configuration required</h1>
        <p>Set the documented Azure Entra ID frontend variables before running the portal.</p>
      </div>
    );
  }

  if (!authenticated) {
    return (
      <div className="auth-status">
        <h1>Sign in to Evidentia</h1>
        <button className="button button-primary" onClick={() => login()}>
          Sign in with Microsoft
        </button>
      </div>
    );
  }

  return children;
};
