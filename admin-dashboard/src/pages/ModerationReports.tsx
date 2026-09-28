import React from 'react';
import { ModerationReport, AdminUser } from '../types/admin';
import {
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
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px', backgroundColor: '#ffffff', borderTop: '4px solid #ef4444' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: '#fef2f2',
            border: '1.5px solid #fecaca',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#ef4444'
          }}>
            <ShieldAlert size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#09090b' }}>Community Safety & Bad Practice Queue</h3>
            <p style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>
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
                padding: '22px 24px',
                backgroundColor: '#ffffff',
                borderLeft: `5px solid ${isPending ? '#ef4444' : '#10b981'}`,
                borderTop: '1px solid #e2e8f0',
                borderRight: '1px solid #e2e8f0',
                borderBottom: '1px solid #e2e8f0',
                opacity: isPending ? 1 : 0.75
              }}
            >
              <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
                <div style={{ display: 'flex', gap: '16px', minWidth: 0, flex: 1 }}>
                  <img
                    src={report.reportedUserAvatar}
                    alt={report.reportedUserName}
                    style={{ width: '48px', height: '48px', borderRadius: '50%', objectFit: 'cover', border: `2px solid ${isPending ? '#fecaca' : '#a7f3d0'}` }}
                  />

                  <div style={{ flex: 1 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                      <h4 style={{ fontSize: '16px', fontWeight: 800, color: '#09090b' }}>{report.reportedUserName}</h4>
                      <span className={`badge badge-${isPending ? 'danger' : 'active'}`}>
                        {report.severity} PRIORITY
                      </span>
                      <span className="badge" style={{ background: '#f8fafc', color: '#334155', border: '1px solid #e2e8f0' }}>
                        {report.type.replace(/_/g, ' ')}
                      </span>
                      <span style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>
                        <Clock size={12} style={{ verticalAlign: 'middle', marginRight: '3px' }} />
                        {report.timestamp}
                      </span>
                    </div>

                    {/* Report body */}
                    <p style={{ fontSize: '14px', color: '#09090b', marginTop: '10px', lineHeight: '1.5', fontWeight: 500 }}>
                      "{report.description}"
                    </p>

                    {/* Target content reference */}
                    <div style={{
                      marginTop: '12px',
                      display: 'inline-flex',
                      alignItems: 'center',
                      gap: '8px',
                      padding: '6px 12px',
                      borderRadius: '8px',
                      background: '#f8fafc',
                      border: '1px solid #e2e8f0',
                      fontSize: '12px',
                      color: '#334155',
                      fontWeight: 600
                    }}>
                      {report.targetType === 'VIDEO' && <FileVideo size={14} color="#10b981" />}
                      {report.targetType === 'CHAT_MESSAGE' && <MessageSquare size={14} color="#ef4444" />}
                      <span>Target: <strong style={{ color: '#09090b' }}>{report.targetContent}</strong></span>
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
                    <span className="badge badge-active" style={{ fontSize: '12px', padding: '6px 14px' }}>
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
