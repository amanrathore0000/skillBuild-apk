import React, { useState, useMemo } from 'react';
import { AdminUser, UserStatus } from '../types/admin';
import {
  Search,
  Filter,
  Ban,
  CheckCircle,
  AlertTriangle,
  Eye,
  Shield,
  Phone,
  MapPin,
  Calendar,
  X
} from 'lucide-react';

interface UserManagementProps {
  users: AdminUser[];
  onOpenBlockModal: (user: AdminUser) => void;
  onUnblockUser: (userId: string) => void;
  onWarnUser: (user: AdminUser) => void;
}

export const UserManagement: React.FC<UserManagementProps> = ({
  users,
  onOpenBlockModal,
  onUnblockUser,
  onWarnUser
}) => {
  const [search, setSearch] = useState('');
  const [roleFilter, setRoleFilter] = useState<'ALL' | 'MENTOR' | 'LEARNER'>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'WARNED' | 'BLOCKED'>('ALL');
  const [selectedUserForDetail, setSelectedUserForDetail] = useState<AdminUser | null>(null);

  const filteredUsers = useMemo(() => {
    return users.filter(user => {
      const matchesSearch = 
        user.name.toLowerCase().includes(search.toLowerCase()) ||
        user.email.toLowerCase().includes(search.toLowerCase()) ||
        user.location.toLowerCase().includes(search.toLowerCase());

      const matchesRole = roleFilter === 'ALL' || user.role === roleFilter;
      const matchesStatus = 
        statusFilter === 'ALL' ||
        (statusFilter === 'BLOCKED' && (user.status === 'BLOCKED' || user.status === 'SUSPENDED')) ||
        user.status === statusFilter;

      return matchesSearch && matchesRole && matchesStatus;
    });
  }, [users, search, roleFilter, statusFilter]);

  return (
    <div>
      {/* Header Actions & Filters */}
      <div className="glass-card" style={{ padding: '20px 24px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          {/* Search bar */}
          <div className="search-input-wrapper" style={{ width: '360px' }}>
            <Search size={16} />
            <input
              type="text"
              className="search-input"
              placeholder="Search by user name, email, or city..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
          </div>

          {/* Filters */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Role:</span>
              <select
                className="select-input"
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value as any)}
              >
                <option value="ALL">All Roles ({users.length})</option>
                <option value="MENTOR">Mentors ({users.filter(u => u.role === 'MENTOR').length})</option>
                <option value="LEARNER">Learners ({users.filter(u => u.role === 'LEARNER').length})</option>
              </select>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Account Status:</span>
              <select
                className="select-input"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as any)}
              >
                <option value="ALL">All Statuses</option>
                <option value="ACTIVE">Active</option>
                <option value="WARNED">Warned for Bad Practice</option>
                <option value="BLOCKED">Blocked / Stopped</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Users Data Table */}
      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <div className="table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>User Details</th>
                <th>Role</th>
                <th>Status & Bad Practice</th>
                <th>Trust Score</th>
                <th>Uploads</th>
                <th>Activity</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '40px', color: 'var(--text-muted)' }}>
                    No users match your filter criteria.
                  </td>
                </tr>
              ) : (
                filteredUsers.map(user => {
                  const isBlocked = user.status === 'BLOCKED' || user.status === 'SUSPENDED';
                  const isWarned = user.status === 'WARNED';

                  return (
                    <tr key={user.id} style={{ background: isBlocked ? 'rgba(239, 68, 68, 0.04)' : undefined }}>
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                          <img
                            src={user.avatarUrl}
                            alt={user.name}
                            style={{
                              width: '42px',
                              height: '42px',
                              borderRadius: '50%',
                              objectFit: 'cover',
                              border: isBlocked ? '2px solid var(--danger)' : '1px solid var(--border-subtle)'
                            }}
                          />
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                              <span style={{ fontWeight: 700, fontSize: '14px' }}>{user.name}</span>
                            </div>
                            <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>{user.email}</p>
                          </div>
                        </div>
                      </td>

                      <td>
                        <span className={`badge badge-${user.role.toLowerCase()}`}>
                          {user.role}
                        </span>
                      </td>

                      <td>
                        <div>
                          <span className={`badge badge-${isBlocked ? 'blocked' : isWarned ? 'warned' : 'active'}`}>
                            {user.status}
                          </span>
                          {user.reasonBlocked && (
                            <p style={{
                              fontSize: '11px',
                              color: isBlocked ? '#fca5a5' : 'var(--warning)',
                              marginTop: '4px',
                              maxWidth: '240px',
                              lineHeight: '1.3'
                            }}>
                              ⚠ {user.reasonBlocked}
                            </p>
                          )}
                        </div>
                      </td>

                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <div style={{
                            width: '48px',
                            height: '6px',
                            borderRadius: '3px',
                            background: 'var(--bg-input)',
                            overflow: 'hidden'
                          }}>
                            <div style={{
                              height: '100%',
                              width: `${user.trustScore}%`,
                              backgroundColor: user.trustScore > 80 ? 'var(--success)' : user.trustScore > 50 ? 'var(--warning)' : 'var(--danger)'
                            }} />
                          </div>
                          <span style={{ fontSize: '12px', fontWeight: 700 }}>{user.trustScore}%</span>
                        </div>
                      </td>

                      <td>
                        <span style={{ fontSize: '13px', fontWeight: 600 }}>
                          {user.uploadsCount} {user.role === 'MENTOR' ? 'Courses' : 'Swaps'}
                        </span>
                      </td>

                      <td>
                        <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                          {user.lastActive}
                        </span>
                      </td>

                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '8px' }}>
                          <button
                            className="btn btn-outline btn-sm"
                            title="Inspect User Details"
                            onClick={() => setSelectedUserForDetail(user)}
                          >
                            <Eye size={13} />
                            Inspect
                          </button>

                          {isBlocked ? (
                            <button
                              className="btn btn-success btn-sm"
                              title="Unblock User Access"
                              onClick={() => onUnblockUser(user.id)}
                            >
                              <CheckCircle size={13} />
                              Unblock
                            </button>
                          ) : (
                            <button
                              className="btn btn-danger btn-sm"
                              title="Block User from Accessing Account on Bad Practice"
                              onClick={() => onOpenBlockModal(user)}
                            >
                              <Ban size={13} />
                              Block User
                            </button>
                          )}
                        </div>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* User Details Slide-over Drawer / Modal */}
      {selectedUserForDetail && (
        <div className="modal-overlay" onClick={() => setSelectedUserForDetail(null)}>
          <div className="modal-container" style={{ maxWidth: '620px' }} onClick={e => e.stopPropagation()}>
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <Shield size={20} color="var(--primary)" />
                <h3 style={{ fontSize: '17px', fontWeight: 800 }}>User Profile & Safety File</h3>
              </div>
              <button className="btn btn-outline" style={{ padding: '6px', borderRadius: '50%' }} onClick={() => setSelectedUserForDetail(null)}>
                <X size={16} />
              </button>
            </div>

            <div className="modal-body">
              {/* Profile Card */}
              <div style={{ display: 'flex', alignItems: 'center', gap: '16px', marginBottom: '20px' }}>
                <img
                  src={selectedUserForDetail.avatarUrl}
                  alt={selectedUserForDetail.name}
                  style={{ width: '64px', height: '64px', borderRadius: '50%', objectFit: 'cover' }}
                />
                <div>
                  <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <h4 style={{ fontSize: '18px', fontWeight: 800 }}>{selectedUserForDetail.name}</h4>
                    <span className={`badge badge-${selectedUserForDetail.role.toLowerCase()}`}>{selectedUserForDetail.role}</span>
                  </div>
                  <p style={{ fontSize: '13px', color: 'var(--text-muted)' }}>{selectedUserForDetail.email}</p>
                  <div style={{ display: 'flex', gap: '12px', marginTop: '6px', fontSize: '12px', color: 'var(--text-secondary)' }}>
                    <span><Phone size={12} style={{ verticalAlign: 'middle', marginRight: '3px' }} /> {selectedUserForDetail.phone}</span>
                    <span><MapPin size={12} style={{ verticalAlign: 'middle', marginRight: '3px' }} /> {selectedUserForDetail.location}</span>
                  </div>
                </div>
              </div>

              {/* Status & Trust Metrics */}
              <div style={{
                display: 'grid',
                gridTemplateColumns: 'repeat(3, 1fr)',
                gap: '12px',
                padding: '14px',
                background: 'rgba(0, 0, 0, 0.25)',
                borderRadius: '12px',
                marginBottom: '20px'
              }}>
                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>STATUS</span>
                  <div style={{ marginTop: '4px' }}>
                    <span className={`badge badge-${selectedUserForDetail.status === 'BLOCKED' ? 'blocked' : selectedUserForDetail.status === 'WARNED' ? 'warned' : 'active'}`}>
                      {selectedUserForDetail.status}
                    </span>
                  </div>
                </div>

                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>TRUST SCORE</span>
                  <p style={{ fontSize: '16px', fontWeight: 800, marginTop: '2px', color: selectedUserForDetail.trustScore > 80 ? 'var(--success)' : 'var(--danger)' }}>
                    {selectedUserForDetail.trustScore}%
                  </p>
                </div>

                <div>
                  <span style={{ fontSize: '11px', color: 'var(--text-muted)' }}>BAD PRACTICE WARNS</span>
                  <p style={{ fontSize: '16px', fontWeight: 800, marginTop: '2px', color: selectedUserForDetail.warningCount > 0 ? 'var(--warning)' : 'inherit' }}>
                    {selectedUserForDetail.warningCount} Strikes
                  </p>
                </div>
              </div>

              {/* Bad practice history */}
              {selectedUserForDetail.reasonBlocked && (
                <div style={{
                  padding: '14px',
                  borderRadius: '10px',
                  background: 'rgba(239, 68, 68, 0.12)',
                  border: '1px solid rgba(239, 68, 68, 0.3)',
                  marginBottom: '20px'
                }}>
                  <strong style={{ fontSize: '13px', color: 'var(--danger)' }}>Enforced Violation Details:</strong>
                  <p style={{ fontSize: '13px', color: '#fca5a5', marginTop: '4px' }}>
                    {selectedUserForDetail.reasonBlocked}
                  </p>
                  {selectedUserForDetail.blockedAt && (
                    <span style={{ fontSize: '11px', color: 'var(--text-muted)', display: 'block', marginTop: '4px' }}>
                      Enforced on: {selectedUserForDetail.blockedAt}
                    </span>
                  )}
                </div>
              )}

              <div style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                <p>Joined platform on {selectedUserForDetail.joinedDate} • Total curriculum uploads: {selectedUserForDetail.uploadsCount} • Completed Swaps: {selectedUserForDetail.swapCount}</p>
              </div>
            </div>

            <div className="modal-footer">
              <button className="btn btn-outline" onClick={() => setSelectedUserForDetail(null)}>
                Close
              </button>
              {selectedUserForDetail.status === 'BLOCKED' || selectedUserForDetail.status === 'SUSPENDED' ? (
                <button
                  className="btn btn-success"
                  onClick={() => {
                    onUnblockUser(selectedUserForDetail.id);
                    setSelectedUserForDetail(null);
                  }}
                >
                  <CheckCircle size={15} />
                  Restore & Unblock User
                </button>
              ) : (
                <button
                  className="btn btn-danger"
                  onClick={() => {
                    const u = selectedUserForDetail;
                    setSelectedUserForDetail(null);
                    onOpenBlockModal(u);
                  }}
                >
                  <Ban size={15} />
                  Stop / Block User Access
                </button>
              )}
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
