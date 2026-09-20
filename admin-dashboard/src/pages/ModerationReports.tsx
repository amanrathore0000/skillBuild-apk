import React from 'react';
import { ModerationReport, AdminUser } from '../types/admin';
import {
  AlertTriangle,
  CheckCircle2,
  XCircle,
  Ban,
  ShieldAlert,
  MessageSquare,
  FileVideo,
  Clock
} from 'lucide-react';

interface ModerationReportsProps {
  reports: ModerationReport[];
  users: AdminUser[];
  onResolveReport: (reportId: string, note: string) => void;
  onDismissReport: (reportId: string) => void;
  onOpenBlockModal: (user: AdminUser) => void;
}

export const ModerationReports: React.FC<ModerationReportsProps> = ({
  reports,
  users,
  onResolveReport,
  onDismissReport,
  onOpenBlockModal
}) => {
  return (
    <div>
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '10px',
            background: 'rgba(245, 158, 11, 0.15)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--warning)'
          }}>
            <ShieldAlert size={22} />
          </div>
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 800 }}>Community Safety & Bad Practice Queue</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              Reports filed against users for fraudulent swaps, copyright strikes, or offensive material.
            </p>
          </div>
        </div>
      </div>

      <div style={{ display: 'flex', flexDirection: 'column', gap: '16px' }}>
        {reports.map(report => {
          const targetUser = users.find(u => u.id === report.reportedUserId);
          const isPending = report.status === 'PENDING';

          return (
            <div
              key={report.id}
              className="glass-card"
              style={{
                padding: '20px 24px',
                borderLeft: `4px solid ${
                  report.severity === 'CRITICAL' ? 'var(--danger)' :
                  report.severity === 'HIGH' ? '#f97316' :
                  report.severity === 'MEDIUM' ? 'var(--warning)' : 'var(--info)'
                }`,
                opacity: isPending ? 1 : 0.65
              }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
                <div style={{ display: 'flex', gap: '16px', minWidth: 0, flex: 1 }}>
                  <img
                    src={report.reportedUserAvatar}
                    alt={report.reportedUserName}
                    style={{ width: '48px', height: '48px', borderRadius: '50%', objectFit: 'cover' }}
                  />

                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                      <h4 style={{ fontSize: '16px', fontWeight: 800 }}>{report.reportedUserName}</h4>
                      <span className={`badge badge-${report.severity === 'CRITICAL' || report.severity === 'HIGH' ? 'blocked' : 'warned'}`}>
                        {report.severity} PRIORITY
                      </span>
                      <span className="badge badge-outline" style={{ border: '1px solid var(--border-subtle)' }}>
                        {report.type.replace(/_/g, ' ')}
                      </span>
                      <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>
                        <Clock size={11} style={{ verticalAlign: 'middle', marginRight: '3px' }} />
                        {report.timestamp}
                      </span>
                    </div>

                    {/* Report body */}
                    <p style={{ fontSize: '14px', color: 'var(--text-primary)', marginTop: '8px', lineHeight: '1.5' }}>
                      "{report.description}"
                    </p>

                    {/* Target content reference */}
                    <div style={{
                      marginTop: '10px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '8px',
                      padding: '4px 10px',
                      borderRadius: '6px',
                      background: 'var(--bg-input)',
                      fontSize: '12px',
                      color: 'var(--text-secondary)'
                    }}>
                      {report.targetType === 'VIDEO' && <FileVideo size={14} color="var(--primary-light)" />}
                      {report.targetType === 'CHAT_MESSAGE' && <MessageSquare size={14} color="var(--warning)" />}
                      <span>Target: <strong>{report.targetContent}</strong></span>
                      <span>• Reported by: {report.reporterName}</span>
                    </div>
                  </div>
                </div>

                {/* Actions */}
                <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                  {isPending ? (
                    <>
                      {targetUser && targetUser.status !== 'BLOCKED' && (
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => onOpenBlockModal(targetUser)}
                        >
                          <Ban size={14} />
                          Block User Access
                        </button>
                      )}
                      <button
                        className="btn btn-success btn-sm"
                        onClick={() => onResolveReport(report.id, 'Investigated and resolved by Administrator.')}
                      >
                        <CheckCircle2 size={14} />
                        Mark Resolved
                      </button>
                      <button
                        className="btn btn-outline btn-sm"
                        onClick={() => onDismissReport(report.id)}
                      >
                        <XCircle size={14} />
                        Dismiss
                      </button>
                    </>
                  ) : (
                    <span className="badge badge-active" style={{ fontSize: '12px', padding: '6px 12px' }}>
                      {report.status}
                    </span>
                  )}
                </div>
              </div>
            </div>
          );
        })}
      </div>
    </div>
  );
};
