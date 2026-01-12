import { useAuditTrail } from '../hooks/useAudit';
import './AuditLog.css';

export const AuditLog = () => {
  const { data: events, isLoading, error } = useAuditTrail(200);

  if (isLoading) {
    return (
      <div className="loading-spinner">
        <div className="spinner"></div>
        <p className="loading-text">Loading audit log...</p>
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
          <strong>Error loading audit log</strong>
          <p>{error.message}</p>
        </div>
      </div>
    );
  }

  if (!events || events.length === 0) {
    return (
      <div className="empty-state">
        <svg className="empty-icon" fill="none" viewBox="0 0 24 24" stroke="currentColor">
          <path strokeLinecap="round" strokeLinejoin="round" strokeWidth={2} d="M9 12h6m-6 4h6m2 5H7a2 2 0 01-2-2V5a2 2 0 012-2h5.586a1 1 0 01.707.293l5.414 5.414a1 1 0 01.293.707V19a2 2 0 01-2 2z" />
        </svg>
        <div className="empty-text">
          <strong>No audit events found</strong>
          <p>Audit events will appear here as actions are performed.</p>
        </div>
      </div>
    );
  }

  return (
    <div className="audit-log">
      <div className="page-header">
        <h1>Audit Log</h1>
        <p className="page-subtitle">View all system activity and changes</p>
      </div>

      <div className="card">
        <div className="table-container">
          <table className="data-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Actor</th>
                <th>Action</th>
                <th>Resource</th>
                <th>Resource ID</th>
              </tr>
            </thead>
            <tbody>
              {events.map((event) => (
                <tr key={event.id}>
                  <td>{new Date(event.timestamp).toLocaleString()}</td>
                  <td>{event.actor}</td>
                  <td>
                    <span className="action-badge">{event.action}</span>
                  </td>
                  <td>{event.resourceType}</td>
                  <td>
                    <code className="resource-id">{event.resourceId}</code>
                  </td>
                </tr>
              ))}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
