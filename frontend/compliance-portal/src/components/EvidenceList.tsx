import { Link } from 'react-router-dom';
import { useEvidenceList } from '../hooks/useEvidence';
import { useState, useMemo } from 'react';
import './EvidenceList.css';

const statusColors: Record<string, string> = {
  DRAFT: 'var(--color-text-tertiary)',
  IN_REVIEW: 'var(--color-warning)',
  APPROVED: 'var(--color-success)',
  REJECTED: 'var(--color-error)',
  LOCKED: 'var(--color-secondary)',
};

export const EvidenceList = () => {
  const { data: evidence, isLoading, error } = useEvidenceList();
  const [searchQuery, setSearchQuery] = useState('');
  const [statusFilter, setStatusFilter] = useState<string>('ALL');

  const filteredEvidence = useMemo(() => {
    if (!evidence) return [];
    
    return evidence.filter(item => {
      const matchesSearch = searchQuery === '' || 
        item.title.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.description.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.type.toLowerCase().includes(searchQuery.toLowerCase()) ||
        item.owner.toLowerCase().includes(searchQuery.toLowerCase());
      
      const matchesStatus = statusFilter === 'ALL' || item.status === statusFilter;
      
      return matchesSearch && matchesStatus;
    });
  }, [evidence, searchQuery, statusFilter]);

  if (isLoading) {
    return (
      <div className="loading-spinner">
        <div className="spinner"></div>
        <p className="loading-text">Loading evidence...</p>
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
          <strong>Error loading evidence</strong>
          <p>{error.message}</p>
        </div>
      </div>
    );
  }

  if (!evidence || evidence.length === 0) {
    return (
      <div className="empty-state">
        <svg className="empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <div className="empty-text">
          <strong>No evidence found</strong>
          <p>Get started by creating your first evidence item.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="evidence-list-container">
      <div className="list-header">
        <h1>Evidence</h1>
        <Link to="/evidence/new" className="create-button-inline">
          <svg width="16" height="16" viewBox="0 0 16 16" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M8 3V13M3 8H13" stroke="currentColor" strokeWidth="2" strokeLinecap="round"/>
          </svg>
          New Evidence
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
                placeholder="Search evidence..."
                value={searchQuery}
                onChange={(e) => setSearchQuery(e.target.value)}
              />
            </div>
            <div className="filter-chips">
              {['ALL', 'DRAFT', 'IN_REVIEW', 'APPROVED', 'REJECTED', 'LOCKED'].map(status => (
                <button
                  key={status}
                  className={`filter-chip ${statusFilter === status ? 'active' : ''}`}
                  onClick={() => setStatusFilter(status)}
                >
                  {status.replace('_', ' ')}
                </button>
              ))}
            </div>
          </div>
          <p className="card-subtitle">
            {filteredEvidence.length} {filteredEvidence.length === 1 ? 'item' : 'items'}
            {searchQuery || statusFilter !== 'ALL' ? ` (filtered from ${evidence?.length || 0})` : ''}
          </p>
        </div>
        <div className="card-body">
          {filteredEvidence.length === 0 ? (
            <div className="empty-state-small">
              <p>No evidence found matching your filters.</p>
            </div>
          ) : (
            <div className="table-container">
              <table className="evidence-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Source System</th>
                    <th>Owner</th>
                    <th>Status</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {filteredEvidence.map((item) => (
                    <tr key={item.id}>
                      <td>
                        <Link to={`/evidence/${item.id}`} className="table-link">
                          {item.title}
                        </Link>
                      </td>
                      <td>
                        <span className="table-cell-content">{item.type}</span>
                      </td>
                      <td>
                        <span className="table-cell-content">{item.sourceSystem}</span>
                      </td>
                      <td>
                        <span className="table-cell-content">{item.owner}</span>
                      </td>
                      <td>
                        <span 
                          className="status-badge"
                          style={{ 
                            backgroundColor: `${statusColors[item.status] || statusColors.DRAFT}15`,
                            color: statusColors[item.status] || statusColors.DRAFT
                          }}
                        >
                          {item.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td>
                        <span className="table-cell-content">
                          {new Date(item.createdAt).toLocaleDateString('en-US', {
                            year: 'numeric',
                            month: 'short',
                            day: 'numeric'
                          })}
                        </span>
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          )}
        </div>
      </div>
    </div>
  );
};
