import { Link } from 'react-router-dom';
import { useIncidents } from '../hooks/useIncidents';
import { useState, useMemo } from 'react';
import './IncidentList.css';

const statusColors: Record<string, string> = {
  OPEN: 'var(--color-warning)',
  RESOLVED: 'var(--color-success)',
  CLOSED: 'var(--color-secondary)',
};

const severityColors: Record<string, string> = {
  LOW: 'var(--color-info)',
  MEDIUM: 'var(--color-warning)',
  HIGH: 'var(--color-error)',
  CRITICAL: 'var(--color-error)',
};

export const IncidentList = () => {
  const [statusFilter, setStatusFilter] = useState<string>('ALL');
  const { data: incidents, isLoading, error } = useIncidents(statusFilter === 'ALL' ? undefined : statusFilter);
  const [searchQuery, setSearchQuery] = useState('');

  const filteredIncidents = useMemo(() => {
    if (!incidents) return [];
    
    return incidents.filter(item => {
      const matchesSearch = searchQuery === '' || 
        item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.severity.toLowerCase().includes(searchQuery.toLowerCase());
      
      return matchesSearch;
    });
  }, [incidents, searchQuery]);

  if (isLoading) {
    return (
      <div className="loading-spinner">
        <div className="spinner"></div>
        <p className="loading-text">Loading incidents...</p>
      </div>
    );
  }

  if (error) {
    return (
      <div className="error-state">
        <svg className="error-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div className="error-message">
          <strong>Error loading incidents</strong>
          <p>{error.message}</p>
        </div>
      </div>
    );
  }

  if (!incidents || incidents.length === 0) {
    return (
      <div className="empty-state">
        <svg className="empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z" />
        </svg>
        <div className="empty-text">
          <strong>No incidents found</strong>
          <p>Get started by creating your first incident.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="incident-list-container">
      <div className="list-header">
        <h1>Incidents</h1>
        <Link to="/incidents/new" className="create-button-inline">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 3V13M3 8H13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
          New Incident
        </Link>
      </div>

      <div className="card">
        <div className="card-header">
          <div className="filters-container">
            <div className="search-container">
              <svg className="search-icon" width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
                <path d="M9 17A8 8 0 1 0 9 1a8 8 0 0 0 0 16zM19 19l-4.35-4.35" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
              </svg>
              <input
                type="text"
                className="search-input"
                placeholder="Search incidents..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="filter-chips">
              {['ALL', 'OPEN', 'RESOLVED', 'CLOSED'].map(status => (
                <button
                  key={status}
                  className={`filter-chip ${statusFilter === status ? 'filter-chip-active' : ''}`}
                  onClick={() => setStatusFilter(status)}
                >
                  {status.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
        </div>

        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Title</th>
                <th>Severity</th>
                <th>Status</th>
                <th>Created</th>
                <th>Created By</th>
              </tr>
            </thead>
            <tbody>
              {filteredIncidents.map((incident) => (
                <tr key={incident.id}>
                  <td>
                    <Link to={`/incidents/${incident.id}`} className="table-link">
                      {incident.title}
                    </Link>
                  </td>
                  <td>
                    <span 
                      className="severity-badge"
                      style={{ backgroundColor: severityColors[incident.severity] || 'var(--color-text-tertiary)' }}
                    >
                      {incident.severity}
                    </span>
                  </td>
                  <td>
                    <span 
                      className="status-badge"
                      style={{ backgroundColor: statusColors[incident.status] || 'var(--color-text-tertiary)' }}
                    >
                      {incident.status}
                    </span>
                  </td>
                  <td>{new Date(incident.createdAt).toLocaleDateString()}</td>
                  <td>{incident.createdBy}</td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>

        {filteredIncidents.length === 0 && searchQuery && (
          <div className="empty-table-state">
            <p>No incidents match your search criteria.</p>
          </div>
        )}
      </div>
    </div>
  );
};
