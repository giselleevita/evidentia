import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { useCreateIncident } from '../hooks/useIncidents';
import { getApiErrorMessage } from '../api/errors';
import './CreateIncident.css';

export const CreateIncident = () => {
  const navigate = useNavigate();
  const createIncident = useCreateIncident();
  const [formData, setFormData] = useState({
    title: '',
    description: '',
    severity: 'MEDIUM',
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    if (!formData.title.trim()) {
      setErrors({ title: 'Title is required' });
      return;
    }

    if (!formData.description.trim()) {
      setErrors({ description: 'Description is required' });
      return;
    }

    try {
      const incident = await createIncident.mutateAsync(formData);
      navigate(`/incidents/${incident.id}`);
    } catch (error: unknown) {
      console.error('Failed to create incident:', error);
      setErrors({ submit: getApiErrorMessage(error, 'Failed to create incident') });
    }
  };

  return (
    <div className="create-incident">
      <div className="page-header">
        <h1>Create Incident</h1>
        <button onClick={() => navigate('/incidents')} className="back-button">
          ← Back to Incidents
        </button>
      </div>

      <div className="card">
        <form onSubmit={handleSubmit} className="form">
          <div className="form-group">
            <label htmlFor="title">
              Title <span className="required">*</span>
            </label>
            <input
              id="title"
              type="text"
              value={formData.title}
              onChange={(e) => setFormData({ ...formData, title: e.target.value })}
              className={errors.title ? 'input-error' : ''}
              maxLength={500}
            />
            {errors.title && <span className="error-text">{errors.title}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="description">
              Description <span className="required">*</span>
            </label>
            <textarea
              id="description"
              value={formData.description}
              onChange={(e) => setFormData({ ...formData, description: e.target.value })}
              className={errors.description ? 'input-error' : ''}
              rows={8}
            />
            {errors.description && <span className="error-text">{errors.description}</span>}
          </div>

          <div className="form-group">
            <label htmlFor="severity">
              Severity <span className="required">*</span>
            </label>
            <select
              id="severity"
              value={formData.severity}
              onChange={(e) => setFormData({ ...formData, severity: e.target.value })}
            >
              <option value="LOW">Low</option>
              <option value="MEDIUM">Medium</option>
              <option value="HIGH">High</option>
              <option value="CRITICAL">Critical</option>
            </select>
          </div>

          {errors.submit && (
            <div className="error-message">
              {errors.submit}
            </div>
          )}

          <div className="form-actions">
            <button
              type="button"
              onClick={() => navigate('/incidents')}
              className="button button-secondary"
            >
              Cancel
            </button>
            <button
              type="submit"
              className="button button-primary"
              disabled={createIncident.isPending}
            >
              {createIncident.isPending ? 'Creating...' : 'Create Incident'}
            </button>
          </div>
        </form>
      </div>
    </div>
  );
};
