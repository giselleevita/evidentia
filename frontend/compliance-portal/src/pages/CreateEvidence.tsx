import { useNavigate } from 'react-router-dom';
import { useState } from 'react';
import { useCreateEvidence } from '../hooks/useEvidence';
import { CreateEvidenceRequest } from '../types/evidence';
import './CreateEvidence.css';

export const CreateEvidence = () => {
  const navigate = useNavigate();
  const createEvidence = useCreateEvidence();
  const [formData, setFormData] = useState<CreateEvidenceRequest>({
    title: '',
    description: '',
    type: '',
    sourceSystem: '',
    owner: '',
    references: {},
  });
  const [errors, setErrors] = useState<Record<string, string>>({});

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setErrors({});

    // Basic validation
    const newErrors: Record<string, string> = {};
    if (!formData.title.trim()) newErrors.title = 'Title is required';
    if (!formData.description.trim()) newErrors.description = 'Description is required';
    if (!formData.type.trim()) newErrors.type = 'Type is required';
    if (!formData.sourceSystem.trim()) newErrors.sourceSystem = 'Source system is required';
    if (!formData.owner.trim()) newErrors.owner = 'Owner is required';

    if (Object.keys(newErrors).length > 0) {
      setErrors(newErrors);
      return;
    }

    try {
      const evidence = await createEvidence.mutateAsync(formData);
      navigate(`/evidence/${evidence.id}`);
    } catch (error: any) {
      console.error('Failed to create evidence:', error);
      setErrors({ submit: error?.response?.data?.error?.message || 'Failed to create evidence' });
    }
  };

  const handleChange = (field: keyof CreateEvidenceRequest, value: string) => {
    setFormData(prev => ({ ...prev, [field]: value }));
    if (errors[field]) {
      setErrors(prev => {
        const newErrors = { ...prev };
        delete newErrors[field];
        return newErrors;
      });
    }
  };

  return (
    <div className="create-evidence">
      <div className="form-header">
        <h1>Create New Evidence</h1>
        <p className="form-subtitle">Fill in the details to create a new evidence item</p>
      </div>

      <div className="card">
        <div className="card-body">
          <form onSubmit={handleSubmit} className="evidence-form">
            {errors.submit && (
              <div className="form-error">
                {errors.submit}
              </div>
            )}

            <div className="form-group">
              <label htmlFor="title" className="form-label">
                Title <span className="required">*</span>
              </label>
              <input
                id="title"
                type="text"
                className={`form-input ${errors.title ? 'error' : ''}`}
                value={formData.title}
                onChange={(e) => handleChange('title', e.target.value)}
                placeholder="Enter evidence title"
                maxLength={500}
              />
              {errors.title && <span className="field-error">{errors.title}</span>}
            </div>

            <div className="form-group">
              <label htmlFor="description" className="form-label">
                Description <span className="required">*</span>
              </label>
              <textarea
                id="description"
                className={`form-textarea ${errors.description ? 'error' : ''}`}
                value={formData.description}
                onChange={(e) => handleChange('description', e.target.value)}
                placeholder="Enter evidence description"
                rows={6}
              />
              {errors.description && <span className="field-error">{errors.description}</span>}
            </div>

            <div className="form-row">
              <div className="form-group">
                <label htmlFor="type" className="form-label">
                  Type <span className="required">*</span>
                </label>
                <input
                  id="type"
                  type="text"
                  className={`form-input ${errors.type ? 'error' : ''}`}
                  value={formData.type}
                  onChange={(e) => handleChange('type', e.target.value)}
                  placeholder="e.g., Policy, Procedure, Audit Report"
                />
                {errors.type && <span className="field-error">{errors.type}</span>}
              </div>

              <div className="form-group">
                <label htmlFor="sourceSystem" className="form-label">
                  Source System <span className="required">*</span>
                </label>
                <input
                  id="sourceSystem"
                  type="text"
                  className={`form-input ${errors.sourceSystem ? 'error' : ''}`}
                  value={formData.sourceSystem}
                  onChange={(e) => handleChange('sourceSystem', e.target.value)}
                  placeholder="e.g., GitHub, Jira, SharePoint"
                />
                {errors.sourceSystem && <span className="field-error">{errors.sourceSystem}</span>}
              </div>
            </div>

            <div className="form-group">
              <label htmlFor="owner" className="form-label">
                Owner <span className="required">*</span>
              </label>
              <input
                id="owner"
                type="text"
                className={`form-input ${errors.owner ? 'error' : ''}`}
                value={formData.owner}
                onChange={(e) => handleChange('owner', e.target.value)}
                placeholder="Enter owner name or email"
              />
              {errors.owner && <span className="field-error">{errors.owner}</span>}
            </div>

            <div className="form-actions">
              <button
                type="button"
                className="action-button action-button-secondary"
                onClick={() => navigate('/evidence')}
                disabled={createEvidence.isPending}
              >
                Cancel
              </button>
              <button
                type="submit"
                className="action-button action-button-primary"
                disabled={createEvidence.isPending}
              >
                {createEvidence.isPending ? 'Creating...' : 'Create Evidence'}
              </button>
            </div>
          </form>
        </div>
      </div>
    </div>
  );
};
