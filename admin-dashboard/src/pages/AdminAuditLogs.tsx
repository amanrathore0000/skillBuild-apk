import React from 'react';
import { AuditLogEntry } from '../types/admin';
import { FileText, ShieldAlert, CheckCircle, Ban, AlertTriangle } from 'lucide-react';

interface AuditLogsProps {
  logs: AuditLogEntry[];
}

export const AdminAuditLogs: React.FC<AuditLogsProps> = ({ logs }) => {
  return (
    <div>
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
          <div style={{
            width: '40px',
            height: '40px',
            borderRadius: '10px',
            background: 'rgba(99, 102, 241, 0.15)',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: 'var(--primary)'
          }}>
            <FileText size={22} />
          </div>
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 800 }}>Administrative Moderation Audit Trail</h3>
            <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>
              Immutable record of all enforcement actions, account bans, content takedowns, and warnings.
            </p>
          </div>
        </div>
      </div>

      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <div className="table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Timestamp</th>
                <th>Administrator</th>
                <th>Enforcement Action</th>
                <th>Target Subject</th>
                <th>Details & Reason</th>
              </tr>
            </thead>
            <tbody>
              {logs.map(log => {
                const isDanger = log.severity === 'DANGER';
                const isWarning = log.severity === 'WARNING';
                const isSuccess = log.severity === 'SUCCESS';

                return (
                  <tr key={log.id}>
                    <td style={{ fontSize: '12px', color: 'var(--text-muted)', whiteSpace: 'nowrap' }}>
                      {log.timestamp}
                    </td>

                    <td>
                      <span style={{ fontWeight: 700, fontSize: '13px' }}>{log.adminName}</span>
                    </td>

                    <td>
                      <span className={`badge badge-${isDanger ? 'blocked' : isWarning ? 'warned' : 'active'}`}>
                        {log.action === 'USER_BLOCKED' && <Ban size={11} />}
                        {log.action === 'USER_WARNED' && <AlertTriangle size={11} />}
                        {log.action === 'REPORT_RESOLVED' && <CheckCircle size={11} />}
                        {log.action.replace(/_/g, ' ')}
                      </span>
                    </td>

                    <td>
                      <strong style={{ fontSize: '13px', color: 'var(--text-primary)' }}>{log.target}</strong>
                    </td>

                    <td style={{ fontSize: '13px', color: 'var(--text-secondary)' }}>
                      {log.details}
                    </td>
                  </tr>
                );
              })}
            </tbody>
          </table>
        </div>
      </div>
    </div>
  );
};
