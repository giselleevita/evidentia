import { useParams, useNavigate, Link } from 'react-router-dom';
import { useEvidence, useSubmitEvidence, useApproveEvidence, useRejectEvidence, useLockEvidence } from '../hooks/useEvidence';
import { useState } from 'react';
import './EvidenceDetail.css';

const statusColors: Record<string, string> = {
  DRAFT: 'var(--color-text-tertiary)',
  IN_REVIEW: 'var(--color-warning)',
  APPROVED: 'var(--color-success)',
  REJECTED: 'var(--color-error)',
  LOCKED: 'var(--color-secondary)',
};

export const EvidenceDetail = () => {
  const { id } = useParams<{ id: string }>();
  const navigate = useNavigate();
  const { data: evidence, isLoading, error } = useEvidence(id || '');
  const submitEvidence = useSubmitEvidence();
  const approveEvidence = useApproveEvidence();
  const rejectEvidence = useRejectEvidence();
  const lockEvidence = useLockEvidence();
  const [actionLoading, setActionLoading] = useState<string | null>(null);

  if (isLoading) {
    return (
      <div className="loading-spinner">
        <div className="spinner"></div>
        <p className="loading-text">Loading evidence...</p>
      </div>
    );
  }

  if (error || !evidence) {
    return (
      <div className="error-state">
        <svg className="error-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M12 8v4m0 4h.01M21 12a9 9 0 11-18 0 9 9 0 0118 0z" />
        </svg>
        <div className="error-message">
          <strong>Error loading evidence</strong>
          <p>{error?.message || 'Evidence not found'}</p>
        </div>
        <Link to="/evidence" className="back-button">Back to Evidence List</Link>
      </div>
    );
  }

  const handleSubmit = async () => {
    if (!id) return;
    setActionLoading('submit');
    try {
      await submitEvidence.mutateAsync({ id, note: undefined });
    } catch (err) {
      console.error('Submit failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  const handleApprove = async () => {
    if (!id) return;
    setActionLoading('approve');
    try {
      await approveEvidence.mutateAsync({ id, note: undefined });
    } catch (err) {
      console.error('Approve failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  const handleReject = async () => {
    if (!id) return;
    const reason = prompt('Please provide a reason for rejection:');
    if (!reason) {
      setActionLoading(null);
      return;
    }
    setActionLoading('reject');
    try {
      await rejectEvidence.mutateAsync({ id, reason });
    } catch (err) {
      console.error('Reject failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  const handleLock = async () => {
    if (!id) return;
    if (!confirm('Are you sure you want to lock this evidence? Locked evidence cannot be modified.')) {
      return;
    }
    setActionLoading('lock');
    try {
      await lockEvidence.mutateAsync(id);
    } catch (err) {
      console.error('Lock failed:', err);
    } finally {
      setActionLoading(null);
    }
  };

  const canEdit = evidence.status === 'DRAFT' || evidence.status === 'REJECTED';
  const canSubmit = evidence.status === 'DRAFT';
  const canApprove = evidence.status === 'IN_REVIEW';
  const canReject = evidence.status === 'IN_REVIEW';
  const canLock = evidence.status === 'APPROVED';

  return (
    <div className="evidence-detail">
      <div className="detail-header">
        <Link to="/evidence" className="back-link">
          <svg width="20" height="20" viewBox="0 0 20 20" fill="none" xmlns="http://www.w3.org/2000/svg">
            <path d="M12.5 15L7.5 10L12.5 5" stroke="currentColor" strokeWidth="2" strokeLinecap="round" strokeLinejoin="round"/>
          </svg>
          Back to Evidence
        </Link>
        <div className="detail-header-actions">
          {canEdit && (
            <Link to={`/evidence/${id}/edit`} className="action-button action-button-secondary">
              Edit
            </Link>
          )}
        </div>
      </div>

      <div className="card">
        <div className="card-header">
          <div>
            <h1 className="detail-title">{evidence.title}</h1>
            <span
              className="status-badge status-badge-large"
              style={{
                backgroundColor: `${statusColors[evidence.status] || statusColors.DRAFT}15`,
                color: statusColors[evidence.status] || statusColors.DRAFT,
              }}
            >
              {evidence.status.replace('_', ' ')}
            </span>
          </div>
        </div>

        <div className="card-body">
          <div className="detail-grid">
            <div className="detail-section">
              <h3>Details</h3>
              <dl className="detail-list">
                <div className="detail-item">
                  <dt>Type</dt>
                  <dd>{evidence.type}</dd>
                </div>
                <div className="detail-item">
                  <dt>Source System</dt>
                  <dd>{evidence.sourceSystem}</dd>
                </div>
                <div className="detail-item">
                  <dt>Owner</dt>
                  <dd>{evidence.owner}</dd>
                </div>
                {evidence.approver && (
                  <div className="detail-item">
                    <dt>Approver</dt>
                    <dd>{evidence.approver}</dd>
                  </div>
                )}
                <div className="detail-item">
                  <dt>Version</dt>
                  <dd>{evidence.version}</dd>
                </div>
                <div className="detail-item">
                  <dt>Created</dt>
                  <dd>{new Date(evidence.createdAt).toLocaleString()}</dd>
                </div>
                <div className="detail-item">
                  <dt>Last Updated</dt>
                  <dd>{new Date(evidence.updatedAt).toLocaleString()}</dd>
                </div>
                {evidence.approvedAt && (
                  <div className="detail-item">
                    <dt>Approved At</dt>
                    <dd>{new Date(evidence.approvedAt).toLocaleString()}</dd>
                  </div>
                )}
              </dl>
            </div>

            <div className="detail-section detail-section-full">
              <h3>Description</h3>
              <p className="detail-description">{evidence.description}</p>
            </div>

            {Object.keys(evidence.references).length > 0 && (
              <div className="detail-section detail-section-full">
                <h3>References</h3>
                <dl className="detail-list">
                  {Object.entries(evidence.references).map(([key, value]) => (
                    <div key={key} className="detail-item">
                      <dt>{key}</dt>
                      <dd>{value}</dd>
                    </div>
                  ))}
                </dl>
              </div>
            )}
          </div>
        </div>
      </div>

      {(canSubmit || canApprove || canReject || canLock) && (
        <div className="card action-card">
          <div className="card-body">
            <h3>Actions</h3>
            <div className="action-buttons">
              {canSubmit && (
                <button
                  className="action-button action-button-primary"
                  onClick={handleSubmit}
                  disabled={actionLoading !== null || submitEvidence.isPending}
                >
                  {submitEvidence.isPending ? 'Submitting...' : 'Submit for Review'}
                </button>
              )}
              {canApprove && (
                <button
                  className="action-button action-button-success"
                  onClick={handleApprove}
                  disabled={actionLoading !== null || approveEvidence.isPending}
                >
                  {approveEvidence.isPending ? 'Approving...' : 'Approve'}
                </button>
              )}
              {canReject && (
                <button
                  className="action-button action-button-error"
                  onClick={handleReject}
                  disabled={actionLoading !== null || rejectEvidence.isPending}
                >
                  {rejectEvidence.isPending ? 'Rejecting...' : 'Reject'}
                </button>
              )}
              {canLock && (
                <button
                  className="action-button action-button-secondary"
                  onClick={handleLock}
                  disabled={actionLoading !== null || lockEvidence.isPending}
                >
                  {lockEvidence.isPending ? 'Locking...' : 'Lock Evidence'}
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
