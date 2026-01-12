import { useEffect } from 'react';
import { Link, useLocation } from 'react-router-dom';
import { logout } from '../auth/auth';
import './Header.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

export const Header = () => {
  const location = useLocation();

  useEffect(() => {
    // Add keyboard shortcuts for easy logout
    const handleKeyDown = (e: KeyboardEvent) => {
      // Ctrl+Q / Cmd+Q - Quick logout
      if ((e.ctrlKey || e.metaKey) && e.key === 'q') {
        e.preventDefault();
        handleLogout();
      }
      // Alt+X - Alternative logout shortcut
      if (e.altKey && e.key === 'x') {
        e.preventDefault();
        handleLogout();
      }
    };

    window.addEventListener('keydown', handleKeyDown);
    return () => window.removeEventListener('keydown', handleKeyDown);
  }, []);

  const handleLogout = () => {
    logout(API_BASE_URL);
  };

  const isActive = (path: string) => location.pathname === path || location.pathname.startsWith(path + '/');

  return (
    <header className="app-header">
      <div className="header-container">
        <Link to="/" className="header-brand">
          <svg className="header-logo" width="32" height="32" viewBox="0 0 32 32" fill="none" xmlns="http://www.w3.org/2000/svg">
            <rect width="32" height="32" rx="6" fill="url(#gradient)"/>
            <path d="M16 8L22 12V20L16 24L10 20V12L16 8Z" fill="white" fillOpacity="0.9"/>
            <path d="M16 12L19 14V18L16 20L13 18V14L16 12Z" fill="white"/>
            <defs>
              <linearGradient id="gradient" x1="0" y1="0" x2="32" y2="32" gradientUnits="userSpaceOnUse">
                <stop stopColor="#2563eb"/>
                <stop offset="1" stopColor="#1e40af"/>
              </linearGradient>
            </defs>
          </svg>
          <div>
            <h1 className="header-title">Evidentia</h1>
            <p className="header-subtitle">Compliance Evidence Management</p>
          </div>
        </Link>
        
        <nav className="header-nav">
          <Link to="/" className={`nav-link ${isActive('/') && location.pathname === '/' ? 'active' : ''}`}>
            Dashboard
          </Link>
          <Link to="/evidence" className={`nav-link ${isActive('/evidence') ? 'active' : ''}`}>
            Evidence
          </Link>
          <Link to="/incidents" className={`nav-link ${isActive('/incidents') ? 'active' : ''}`}>
            Incidents
          </Link>
          <Link to="/audit" className={`nav-link ${isActive('/audit') ? 'active' : ''}`}>
            Audit Log
          </Link>
          <Link to="/ratings" className={`nav-link ${isActive('/ratings') ? 'active' : ''}`}>
            Ratings
          </Link>
        </nav>
        
        <div className="header-actions">
          <Link to="/evidence/new" className="create-button">
            <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M8 3V13M3 8H13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
            </svg>
            <span>New Evidence</span>
          </Link>
          <button
            onClick={handleLogout}
            className="logout-button"
            title="Logout (Ctrl+Q / Alt+X)"
          >
            <svg className="logout-icon" width="18" height="18" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
              <path d="M7 2H4C3.44772 2 3 2.44772 3 3V15C3 15.5523 3.44772 16 4 16H7M12 13L15 10M15 10L12 7M15 10H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
            </svg>
            <span>Exit</span>
          </button>
        </div>
      </div>
    </header>
  );
};
