import React from 'react';
import { AuditLogEntry } from '../types/admin';
import { FileText } from 'lucide-react';

interface AuditLogsProps {
  logs: AuditLogEntry[];
}

export const AdminAuditLogs: React.FC<AuditLogsProps> = ({ logs }) => {
  return (
    <div>
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px', backgroundColor: '#ffffff', borderTop: '4px solid #10b981' }}>
        <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
          <div style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: '#ecfdf5',
            border: '1.5px solid #a7f3d0',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            color: '#10b981'
          }}>
            <FileText size={24} />
          </div>
          <div>
            <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#09090b' }}>Administrative Moderation Audit Trail</h3>
            <p style={{ fontSize: '13px', color: '#64748b', fontWeight: 500 }}>
              Immutable record of all enforcement actions, account bans, content takedowns, and warnings.
            </p>
          </div>
        </div>
      </div>

      <div className="glass-card" style={{ overflow: 'hidden', backgroundColor: '#ffffff' }}>
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
                const isDanger = log.severity === 'DANGER' || log.severity === 'WARNING';

                return (
                  <tr key={log.id}>
                    <td style={{ fontSize: '12px', color: '#64748b', whiteSpace: 'nowrap', fontWeight: 500 }}>
                      {log.timestamp}
                    </td>

                    <td>
                      <span style={{ fontWeight: 800, fontSize: '13px', color: '#09090b' }}>{log.adminName}</span>
                    </td>

                    <td>
                      <span className={`badge badge-${isDanger ? 'danger' : 'active'}`} style={{ fontSize: '10px' }}>
                        {log.action}
                      </span>
                    </td>

                    <td>
                      <span style={{ fontWeight: 700, fontSize: '13px', color: '#09090b' }}>
                        {log.target}
                      </span>
                    </td>

                    <td style={{ fontSize: '13px', color: '#334155', fontWeight: 500 }}>
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
