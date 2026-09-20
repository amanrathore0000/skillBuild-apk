import React, { useState } from 'react';
import { AdminUser, UserStatus } from '../../types/admin';
import { AlertOctagon, X, ShieldAlert, Ban, Clock, AlertTriangle } from 'lucide-react';

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
              width: '36px',
              height: '36px',
              borderRadius: '10px',
              backgroundColor: 'rgba(239, 68, 68, 0.15)',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              color: 'var(--danger)'
            }}>
              <AlertOctagon size={20} />
            </div>
            <div>
              <h3 style={{ fontSize: '17px', fontWeight: 800 }}>Account Moderation & Access Control</h3>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Action against bad practice or policy violation</p>
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
            padding: '14px',
            background: 'rgba(0, 0, 0, 0.25)',
            borderRadius: '12px',
            border: '1px solid var(--border-subtle)',
            marginBottom: '20px'
          }}>
            <img
              src={user.avatarUrl}
              alt={user.name}
              style={{ width: '48px', height: '48px', borderRadius: '50%', objectFit: 'cover' }}
            />
            <div style={{ flex: 1, minWidth: 0 }}>
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                <span style={{ fontWeight: 700, fontSize: '15px' }}>{user.name}</span>
                <span className={`badge badge-${user.role.toLowerCase()}`}>{user.role}</span>
              </div>
              <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{user.email}</p>
              <div style={{ display: 'flex', gap: '12px', marginTop: '4px', fontSize: '11px', color: 'var(--text-secondary)' }}>
                <span>Trust Score: <strong>{user.trustScore}%</strong></span>
                <span>Uploads: <strong>{user.uploadsCount}</strong></span>
                <span>Past Warnings: <strong style={{ color: user.warningCount > 0 ? 'var(--warning)' : 'inherit' }}>{user.warningCount}</strong></span>
              </div>
            </div>
          </div>

          {/* Action Selector */}
          <div style={{ marginBottom: '18px' }}>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, marginBottom: '8px' }}>
              Select Moderation Action
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setActionType('BLOCK')}
                style={{
                  padding: '12px 10px',
                  borderRadius: '10px',
                  border: `1.5px solid ${actionType === 'BLOCK' ? 'var(--danger)' : 'var(--border-subtle)'}`,
                  background: actionType === 'BLOCK' ? 'rgba(239, 68, 68, 0.15)' : 'var(--bg-input)',
                  color: actionType === 'BLOCK' ? '#fff' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <Ban size={20} color={actionType === 'BLOCK' ? 'var(--danger)' : 'currentColor'} />
                <span style={{ fontSize: '13px', fontWeight: 700 }}>Block Account</span>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>Revoke Access</span>
              </button>

              <button
                type="button"
                onClick={() => setActionType('SUSPEND')}
                style={{
                  padding: '12px 10px',
                  borderRadius: '10px',
                  border: `1.5px solid ${actionType === 'SUSPEND' ? 'var(--warning)' : 'var(--border-subtle)'}`,
                  background: actionType === 'SUSPEND' ? 'rgba(245, 158, 11, 0.15)' : 'var(--bg-input)',
                  color: actionType === 'SUSPEND' ? '#fff' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <Clock size={20} color={actionType === 'SUSPEND' ? 'var(--warning)' : 'currentColor'} />
                <span style={{ fontSize: '13px', fontWeight: 700 }}>Suspend</span>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>Temporary Hold</span>
              </button>

              <button
                type="button"
                onClick={() => setActionType('WARN')}
                style={{
                  padding: '12px 10px',
                  borderRadius: '10px',
                  border: `1.5px solid ${actionType === 'WARN' ? 'var(--info)' : 'var(--border-subtle)'}`,
                  background: actionType === 'WARN' ? 'rgba(14, 165, 233, 0.15)' : 'var(--bg-input)',
                  color: actionType === 'WARN' ? '#fff' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  display: 'flex',
                  flexDirection: 'column',
                  alignItems: 'center',
                  gap: '6px'
                }}
              >
                <AlertTriangle size={20} color={actionType === 'WARN' ? 'var(--info)' : 'currentColor'} />
                <span style={{ fontSize: '13px', fontWeight: 700 }}>Issue Warning</span>
                <span style={{ fontSize: '10px', color: 'var(--text-muted)' }}>Deduct Trust</span>
              </button>
            </div>
          </div>

          {/* If Suspend: Duration */}
          {actionType === 'SUSPEND' && (
            <div style={{ marginBottom: '18px' }}>
              <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, marginBottom: '6px' }}>
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
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, marginBottom: '6px' }}>
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
                    padding: '10px 12px',
                    borderRadius: '8px',
                    background: selectedReason === reason ? 'rgba(99, 102, 241, 0.12)' : 'var(--bg-input)',
                    border: `1px solid ${selectedReason === reason ? 'var(--primary)' : 'var(--border-subtle)'}`,
                    cursor: 'pointer',
                    fontSize: '13px',
                    transition: 'all 0.15s'
                  }}
                >
                  <input
                    type="radio"
                    name="bad_practice_reason"
                    checked={selectedReason === reason}
                    onChange={() => setSelectedReason(reason)}
                    style={{ marginTop: '2px', accentColor: 'var(--primary)' }}
                  />
                  <span>{reason}</span>
                </label>
              ))}
            </div>
          </div>

          {/* Additional Notes */}
          <div>
            <label style={{ display: 'block', fontSize: '13px', fontWeight: 700, marginBottom: '6px' }}>
              Admin Moderation Notes & Incident Reference (Optional)
            </label>
            <textarea
              rows={3}
              placeholder="E.g., Investigation completed, cross-referenced with report #rep_1. User informed via email."
              value={customNote}
              onChange={(e) => setCustomNote(e.target.value)}
              style={{
                width: '100%',
                background: 'var(--bg-input)',
                border: '1px solid var(--border-subtle)',
                borderRadius: '8px',
                color: 'var(--text-primary)',
                padding: '10px 12px',
                fontFamily: 'inherit',
                fontSize: '13px',
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
            className={`btn ${actionType === 'BLOCK' ? 'btn-danger' : actionType === 'SUSPEND' ? 'btn-warning' : 'btn-primary'}`}
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
