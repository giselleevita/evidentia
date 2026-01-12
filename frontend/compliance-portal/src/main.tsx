import React from 'react';
import ReactDOM from 'react-dom/client';
import App from './App';
import './index.css';
import { logout } from './auth/auth';

// Make logout globally available for easy access (useful for debugging)
const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';
(window as any).logout = () => logout(API_BASE_URL);

ReactDOM.createRoot(document.getElementById('root')!).render(
  <React.StrictMode>
    <App />
  </React.StrictMode>
);
