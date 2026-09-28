import React, { useState } from 'react';
import { AdminUser, UserStatus } from '../../types/admin';
import { AlertOctagon, X, Ban, Clock, AlertTriangle } from 'lucide-react';

interface BlockUserModalProps {
  user: AdminUser | null;
  isOpen: boolean;
  onClose: () => void;
  onConfirm: (userId: string, newStatus: UserStatus, reason: string) => void;
}

const BAD_PRACTICE_PRESETS = [
  'Uploaded copyrighted video course without authorization (DMCA violation)',
  'Inappropriate or offensive video/course content',
  'Harassment or abusive conduct in peer encrypted doubt chat',
  'Fraudulent skill swap behavior / fake completed sessions',
  'Spamming promotional, phishing, or scam links in curriculum',
  'Repeated violation of SkillBuilder mentor guidelines'
];

export const BlockUserModal: React.FC<BlockUserModalProps> = ({
  user,
  isOpen,
  onClose,
  onConfirm
}) => {
  if (!isOpen || !user) return null;

  const [actionType, setActionType] = useState<'BLOCK' | 'SUSPEND' | 'WARN'>('BLOCK');
  const [selectedReason, setSelectedReason] = useState<string>(BAD_PRACTICE_PRESETS[0]);
  const [customNote, setCustomNote] = useState<string>('');
  const [suspensionDuration, setSuspensionDuration] = useState<string>('7 Days');

  const handleApply = () => {
    const fullReason = customNote.trim() 
      ? `${selectedReason} — Note: ${customNote.trim()}`
      : selectedReason;

    const finalStatus: UserStatus = 
      actionType === 'BLOCK' ? 'BLOCKED' :
      actionType === 'SUSPEND' ? 'SUSPENDED' : 'WARNED';

    onConfirm(user.id, finalStatus, fullReason);
    onClose();
  };

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div className="modal-container" onClick={(e) => e.stopPropagation()}>
        {/* Header */}
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            <div style={{
              width: '38px',
              height: '38px',
              borderRadius: '10px',
              backgroundColor: '#fef2f2',
              border: '1px solid #fecaca',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: '#ef4444'
            }}>
              <AlertOctagon size={20} />
            </div>
            <div>
              <h3 style={{ fontSize: '17px', fontWeight: 800, color: '#09090b' }}>Account Moderation & Access Control</h3>
              <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>Action against bad practice or policy violation</p>
            </div>
          </div>
          <button
            onClick={onClose}
            className="btn btn-outline"
            style={{ padding: '6px', borderRadius: '50%' }}
          >
            <X size={16} />
          </button>
        </div>

        {/* Body */}
        <div className="modal-body" style={{ maxHeight: '75vh', overflowY: 'auto' }}>
          {/* Target User Summary Card */}
          <div style={{
            display: 'flex',
            alignItems: 'center',
            gap: '14px',
            padding: '16px',
            background: '#f8fafc',
            borderRadius: '12px',
            border: '1px solid #e2e8f0',
            marginBottom: '20px'
          }}>
            <img
              src={user.avatarUrl}
              alt={user.name}
              style={{ width: '48px', height: '48px', borderRadius: '50%', objectFit: 'cover', border: '2px solid #e2e8f0' }}
            />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontWeight: 800, fontSize: '15px', color: '#09090b' }}>{user.name}</span>
                <span className="badge badge-active">{user.role}</span>
              </div>
              <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>{user.email}</p>
              <div style={{ display: 'flex', gap: '14px', marginTop: '6px', fontSize: '12px', color: '#334155', fontWeight: 600 }}>
                <span>Trust Score: <strong>{user.trustScore}%</strong></span>
                <span>Uploads: <strong>{user.uploadsCount}</strong></span>
                <span>Warnings: <strong style={{ color: user.warningCount > 0 ? '#b91c1c' : 'inherit' }}>{user.warningCount}</strong></span>
              </div>
            </div>
          </div>

          {/* Action Selector */}
          <div style={{ marginBottom: '18px' }}>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 800, color: '#09090b', marginBottom: '8px' }}>
              Select Moderation Action
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setActionType('BLOCK')}
                style={{
                  padding: '12px 10px',
                  borderRadius: '10px',
                  border: `2px solid ${actionType === 'BLOCK' ? '#ef4444' : '#e2e8f0'}`,
                  background: actionType === 'BLOCK' ? '#fef2f2' : '#ffffff',
                  color: actionType === 'BLOCK' ? '#b91c1c' : '#334155',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px',
                  boxShadow: actionType === 'BLOCK' ? '0 2px 8px rgba(239, 68, 68, 0.15)' : 'none'
                }}
              >
                <Ban size={20} color="#ef4444" />
                <span style={{ fontSize: '13px', fontWeight: 800 }}>Block Account</span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>Revoke Access</span>
              </button>

              <button
                type="button"
                onClick={() => setActionType('SUSPEND')}
                style={{
                  padding: '12px 10px',
                  borderRadius: '10px',
                  border: `2px solid ${actionType === 'SUSPEND' ? '#ef4444' : '#e2e8f0'}`,
                  background: actionType === 'SUSPEND' ? '#fef2f2' : '#ffffff',
                  color: actionType === 'SUSPEND' ? '#b91c1c' : '#334155',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px',
                  boxShadow: actionType === 'SUSPEND' ? '0 2px 8px rgba(239, 68, 68, 0.15)' : 'none'
                }}
              >
                <Clock size={20} color="#ef4444" />
                <span style={{ fontSize: '13px', fontWeight: 800 }}>Suspend</span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>Temporary Hold</span>
              </button>

              <button
                type="button"
                onClick={() => setActionType('WARN')}
                style={{
                  padding: '12px 10px',
                  borderRadius: '10px',
                  border: `2px solid ${actionType === 'WARN' ? '#ef4444' : '#e2e8f0'}`,
                  background: actionType === 'WARN' ? '#fef2f2' : '#ffffff',
                  color: actionType === 'WARN' ? '#b91c1c' : '#334155',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px',
                  boxShadow: actionType === 'WARN' ? '0 2px 8px rgba(239, 68, 68, 0.15)' : 'none'
                }}
              >
                <AlertTriangle size={20} color="#ef4444" />
                <span style={{ fontSize: '13px', fontWeight: 800 }}>Issue Warning</span>
                <span style={{ fontSize: '10px', color: '#64748b' }}>Deduct Trust</span>
              </button>
            </div>
          </div>

          {/* If Suspend: Duration */}
          {actionType === 'SUSPEND' && (
            <div style={{ marginBottom: '18px' }}>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 800, color: '#09090b', marginBottom: '6px' }}>
                Suspension Duration
              </label>
              <select
                className="select-input"
                style={{ width: '100%' }}
                value={suspensionDuration}
                onChange={(e) => setSuspensionDuration(e.target.value)}
              >
                <option value="24 Hours">24 Hours (Cooldown)</option>
                <option value="7 Days">7 Days (Standard Suspension)</option>
                <option value="30 Days">30 Days (Extended Review)</option>
              </select>
            </div>
          )}

          {/* Bad Practice Reason Preset */}
          <div style={{ marginBottom: '18px' }}>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 800, color: '#09090b', marginBottom: '6px' }}>
              Bad Practice Violation Reason
            </label>
            <div style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
              {BAD_PRACTICE_PRESETS.map((reason, idx) => (
                <label
                  key={idx}
                  style={{
                    display: 'flex',
                    alignItems: 'flex-start',
                    gap: '10px',
                    padding: '10px 14px',
                    borderRadius: '8px',
                    background: selectedReason === reason ? '#ecfdf5' : '#ffffff',
                    border: `1.5px solid ${selectedReason === reason ? '#10b981' : '#e2e8f0'}`,
                    cursor: 'pointer',
                    fontSize: '13px',
                    fontWeight: selectedReason === reason ? 700 : 500,
                    color: selectedReason === reason ? '#047857' : '#09090b',
                    transition: 'all 0.15s'
                  }}
                >
                  <input
                    type="radio"
                    name="bad_practice_reason"
                    checked={selectedReason === reason}
                    onChange={() => setSelectedReason(reason)}
                    style={{ marginTop: '2px', accentColor: '#10b981' }}
                  />
                  <span>{reason}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Additional Notes */}
          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 800, color: '#09090b', marginBottom: '6px' }}>
              Admin Moderation Notes & Incident Reference (Optional)
            </label>
            <textarea
              rows={3}
              placeholder="E.g., Investigation completed, cross-referenced with report #rep_1. User informed via email."
              value={customNote}
              onChange={(e) => setCustomNote(e.target.value)}
              style={{
                width: '100%',
                background: '#ffffff',
                border: '1.5px solid #e2e8f0',
                borderRadius: '8px',
                color: '#09090b',
                padding: '10px 14px',
                fontFamily: 'inherit',
                fontSize: '13px',
                fontWeight: 500,
                outline: 'none',
                resize: 'vertical'
              }}
            />
          </div>
        </div>

        {/* Footer */}
        <div className="modal-footer">
          <button className="btn btn-outline" onClick={onClose}>
            Cancel
          </button>
          <button
            className="btn btn-danger"
            onClick={handleApply}
          >
            {actionType === 'BLOCK' && <Ban size={16} />}
            {actionType === 'SUSPEND' && <Clock size={16} />}
            {actionType === 'WARN' && <AlertTriangle size={16} />}
            Confirm {actionType === 'BLOCK' ? 'Block Access' : actionType === 'SUSPEND' ? `Suspend (${suspensionDuration})` : 'Warning'}
          </button>
        </div>
      </div>
    </div>
  );
};
