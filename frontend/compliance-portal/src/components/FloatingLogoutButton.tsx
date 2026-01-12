import { useState } from 'react';
import { logout } from '../auth/auth';
import './FloatingLogoutButton.css';

const API_BASE_URL = import.meta.env.VITE_API_BASE_URL || '/api/v1';

/**
 * Floating logout button that's always accessible in the bottom-right corner
 * Provides a quick exit option from anywhere in the app
 */
export const FloatingLogoutButton = () => {
  const [isHovered, setIsHovered] = useState(false);

  const handleLogout = () => {
    logout(API_BASE_URL);
  };

  return (
    <button
      className={`floating-logout-button ${isHovered ? 'hovered' : ''}`}
      onClick={handleLogout}
      onMouseEnter={() => setIsHovered(true)}
      onMouseLeave={() => setIsHovered(false)}
      title="Quick Exit - Click to logout (Ctrl+Q / Alt+X)"
      aria-label="Logout"
    >
      <svg className="logout-icon-small" width="20" height="20" viewBox="0 0 18 18" fill="none" xmlns="http://www.w3.org/2000/svg">
        <path d="M7 2H4C3.44772 2 3 2.44772 3 3V15C3 15.5523 3.44772 16 4 16H7M12 13L15 10M15 10L12 7M15 10H6" stroke="currentColor" strokeWidth="1.5" strokeLinecap="round" strokeLinejoin="round"/>
      </svg>
      {isHovered && (
        <span className="floating-logout-text">EXIT</span>
      )}
    </button>
  );
};
