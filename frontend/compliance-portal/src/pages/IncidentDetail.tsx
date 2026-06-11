import { useParams, Link } from 'react-router-dom';
import { useIncident, useEscalateIncident, useResolveIncident, useReviewIncident } from '../hooks/useIncidents';
import { useState } from 'react';
import './IncidentDetail.css';

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

export const IncidentDetail = () => {
  const { id } = useParams<{ id: string }>();
  const { data: incident, isLoading, error } = useIncident(id || '');
  const escalateIncident = useEscalateIncident();
  const resolveIncident = useResolveIncident();
  const reviewIncident = useReviewIncident();
  const [actionLoading, setActionLoading] = useState<string | null>(null);
  const [note, setNote] = useState('');

  if (isLoading) {
    return (
      <div className="loading-spinner">
        <div className="spinner"></div>
        <p className="loading-text">Loading incident...</p>
      </div>
    );
  }

  if (error || !incident) {
    return (
      <div className="error-state">
        <svg className="error-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div className="error-message">
          <strong>Error loading incident</strong>
          <p>{error?.message || 'Incident not found'}</p>
        </div>
        <Link to="/incidents" className="back-button">Back to Incidents</Link>
      </div>
    );
  }

  const handleEscalate = async () => {
    if (!id || !note.trim()) {
      alert('Please provide an escalation note');
      return;
    }
    setActionLoading('escalate');
    try {
      await escalateIncident.mutateAsync({ id, data: { escalationNote: note } });
      setNote('');
    } catch (err) {
      console.error('Escalate failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  const handleResolve = async () => {
    if (!id || !note.trim()) {
      alert('Please provide a resolution note');
      return;
    }
    setActionLoading('resolve');
    try {
      await resolveIncident.mutateAsync({ id, data: { resolutionNote: note } });
      setNote('');
    } catch (err) {
      console.error('Resolve failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  const handleReview = async () => {
    if (!id || !note.trim()) {
      alert('Please provide review notes');
      return;
    }
    setActionLoading('review');
    try {
      await reviewIncident.mutateAsync({ id, data: { reviewNotes: note } });
      setNote('');
    } catch (err) {
      console.error('Review failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  return (
    <div className="incident-detail">
      <div className="detail-header">
        <div>
          <Link to="/incidents" className="back-link">← Back to Incidents</Link>
          <h1>{incident.title}</h1>
          <div className="header-meta">
            <span 
              className="status-badge status-badge-large"
              style={{ backgroundColor: statusColors[incident.status] || 'var(--color-text-tertiary)' }}
            >
              {incident.status}
            </span>
            <span 
              className="severity-badge severity-badge-large"
              style={{ backgroundColor: severityColors[incident.severity] || 'var(--color-text-tertiary)' }}
            >
              {incident.severity}
            </span>
          </div>
        </div>
      </div>

      <div className="card">
        <div className="detail-section">
          <h2>Description</h2>
          <p className="detail-text">{incident.description}</p>
        </div>

        <div className="detail-section">
          <h2>Details</h2>
          <dl className="detail-list">
            <div className="detail-item">
              <dt>Created</dt>
              <dd>{new Date(incident.createdAt).toLocaleString()}</dd>
            </div>
            <div className="detail-item">
              <dt>Created By</dt>
              <dd>{incident.createdBy}</dd>
            </div>
            {incident.resolvedAt && (
              <div className="detail-item">
                <dt>Resolved</dt>
                <dd>{new Date(incident.resolvedAt).toLocaleString()}</dd>
              </div>
            )}
            {incident.resolvedBy && (
              <div className="detail-item">
                <dt>Resolved By</dt>
                <dd>{incident.resolvedBy}</dd>
              </div>
            )}
            {incident.reviewedAt && (
              <div className="detail-item">
                <dt>Reviewed</dt>
                <dd>{new Date(incident.reviewedAt).toLocaleString()}</dd>
              </div>
            )}
            {incident.reviewedBy && (
              <div className="detail-item">
                <dt>Reviewed By</dt>
                <dd>{incident.reviewedBy}</dd>
              </div>
            )}
            {incident.escalationNote && (
              <div className="detail-item">
                <dt>Escalation Note</dt>
                <dd>{incident.escalationNote}</dd>
              </div>
            )}
            {incident.reviewNotes && (
              <div className="detail-item">
                <dt>Review Notes</dt>
                <dd>{incident.reviewNotes}</dd>
              </div>
            )}
          </dl>
        </div>

        {(incident.status === 'OPEN' || incident.status === 'RESOLVED') && (
          <div className="detail-section">
            <h2>Actions</h2>
            <div className="actions-container">
              <textarea
                placeholder="Enter note..."
                value={note}
                onChange={(e) => setNote(e.target.value)}
                className="action-note-input"
                rows={3}
              />
              <div className="action-buttons">
                {incident.status === 'OPEN' && (
                  <>
                    <button
                      onClick={handleEscalate}
                      disabled={actionLoading !== null || !note.trim()}
                      className="button button-secondary"
                    >
                      {actionLoading === 'escalate' ? 'Escalating...' : 'Escalate'}
                    </button>
                    <button
                      onClick={handleResolve}
                      disabled={actionLoading !== null || !note.trim()}
                      className="button button-primary"
                    >
                      {actionLoading === 'resolve' ? 'Resolving...' : 'Resolve'}
                    </button>
                  </>
                )}
                {incident.status === 'RESOLVED' && (
                  <button
                    onClick={handleReview}
                    disabled={actionLoading !== null || !note.trim()}
                    className="button button-primary"
                  >
                    {actionLoading === 'review' ? 'Reviewing...' : 'Review & Close'}
                  </button>
                )}
              </div>
            </div>
          </div>
        )}
      </div>
    </div>
  );
};
