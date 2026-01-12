import { useEvidenceList } from '../hooks/useEvidence';
import './Dashboard.css';

export const Dashboard = () => {
  const { data: evidence, isLoading } = useEvidenceList();

  if (isLoading) {
    return (
      <div className="loading-spinner">
        <div className="spinner"></div>
        <p className="loading-text">Loading dashboard...</p>
      </div>
    );
  }

  const stats = {
    total: evidence?.length || 0,
    draft: evidence?.filter(e => e.status === 'DRAFT').length || 0,
    inReview: evidence?.filter(e => e.status === 'IN_REVIEW').length || 0,
    approved: evidence?.filter(e => e.status === 'APPROVED').length || 0,
    rejected: evidence?.filter(e => e.status === 'REJECTED').length || 0,
    locked: evidence?.filter(e => e.status === 'LOCKED').length || 0,
  };

  return (
    <div className="dashboard">
      <div className="dashboard-header">
        <h1>Dashboard</h1>
        <p className="dashboard-subtitle">Overview of your evidence management</p>
      </div>

      <div className="stats-grid">
        <div className="stat-card">
          <div className="stat-value">{stats.total}</div>
          <div className="stat-label">Total Evidence</div>
        </div>
        <div className="stat-card">
          <div className="stat-value">{stats.draft}</div>
          <div className="stat-label">Draft</div>
        </div>
        <div className="stat-card stat-card-warning">
          <div className="stat-value">{stats.inReview}</div>
          <div className="stat-label">In Review</div>
        </div>
        <div className="stat-card stat-card-success">
          <div className="stat-value">{stats.approved}</div>
          <div className="stat-label">Approved</div>
        </div>
        <div className="stat-card stat-card-error">
          <div className="stat-value">{stats.rejected}</div>
          <div className="stat-label">Rejected</div>
        </div>
        <div className="stat-card stat-card-secondary">
          <div className="stat-value">{stats.locked}</div>
          <div className="stat-label">Locked</div>
        </div>
      </div>

      {evidence && evidence.length > 0 && (
        <div className="recent-section">
          <h2>Recent Evidence</h2>
          <div className="card">
            <div className="card-body">
              <table className="evidence-table">
                <thead>
                  <tr>
                    <th>Title</th>
                    <th>Type</th>
                    <th>Status</th>
                    <th>Owner</th>
                    <th>Created</th>
                  </tr>
                </thead>
                <tbody>
                  {evidence.slice(0, 5).map((item) => (
                    <tr key={item.id}>
                      <td>
                        <a href={`/evidence/${item.id}`} className="table-link">
                          {item.title}
                        </a>
                      </td>
                      <td>{item.type}</td>
                      <td>
                        <span className="status-badge status-badge-small">
                          {item.status.replace('_', ' ')}
                        </span>
                      </td>
                      <td>{item.owner}</td>
                      <td>
                        {new Date(item.createdAt).toLocaleDateString('en-US', {
                          month: 'short',
                          day: 'numeric',
                          year: 'numeric',
                        })}
                      </td>
                    </tr>
                  ))}
                </tbody>
              </table>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
