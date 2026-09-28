import React, { useState, useMemo } from 'react';
import { AdminUser, UserStatus } from '../types/admin';
import {
  Search,
  Ban,
  CheckCircle,
  Eye,
  Shield,
  Phone,
  MapPin,
  X,
  TrendingUp,
  AlertTriangle,
  Clock,
  DollarSign,
  GraduationCap,
  Users as UsersIcon,
  ShoppingBag,
  KeyRound,
  ShieldAlert,
  Download
} from 'lucide-react';
import { UserDetailsModal } from '../components/users/UserDetailsModal';
import { MentorAnalyticsDrawer } from '../components/users/MentorAnalyticsDrawer';
import { ExportUserModal } from '../components/users/ExportUserModal';

interface UserManagementProps {
  users: AdminUser[];
  onOpenBlockModal: (user: AdminUser) => void;
  onUnblockUser: (userId: string) => void;
  onWarnUser: (user: AdminUser) => void;
  onSuspendUser?: (user: AdminUser) => void;
  onDirectModerate?: (userId: string, status: UserStatus, reason: string) => void;
}

export const UserManagement: React.FC<UserManagementProps> = ({
  users,
  onOpenBlockModal,
  onUnblockUser,
  onWarnUser,
  onSuspendUser,
  onDirectModerate
}) => {
  const [search, setSearch] = useState('');
  const [activeTab, setActiveTab] = useState<'MENTOR' | 'LEARNER' | 'ALL'>('MENTOR');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'WARNED' | 'BLOCKED'>('ALL');
  
  // Modals & Drawers state
  const [selectedUserForDetail, setSelectedUserForDetail] = useState<AdminUser | null>(null);
  const [selectedMentorForAnalytics, setSelectedMentorForAnalytics] = useState<AdminUser | null>(null);
  const [isExportModalOpen, setIsExportModalOpen] = useState<boolean>(false);

  const mentorCount = users.filter(u => u.role === 'MENTOR').length;
  const learnerCount = users.filter(u => u.role === 'LEARNER').length;

  const filteredUsers = useMemo(() => {
    return users.filter(user => {
      const q = search.toLowerCase().trim();
      const matchesSearch =
        !q ||
        user.name.toLowerCase().includes(q) ||
        user.email.toLowerCase().includes(q) ||
        (user.loginId && user.loginId.toLowerCase().includes(q)) ||
        (user.phone && user.phone.toLowerCase().includes(q)) ||
        (user.location && user.location.toLowerCase().includes(q));

      const matchesTab = activeTab === 'ALL' || user.role === activeTab;
      const matchesStatus =
        statusFilter === 'ALL' ||
        (statusFilter === 'BLOCKED' && (user.status === 'BLOCKED' || user.status === 'SUSPENDED')) ||
        user.status === statusFilter;

      return matchesSearch && matchesTab && matchesStatus;
    });
  }, [users, search, activeTab, statusFilter]);

  const handleModerateFromModal = (user: AdminUser, action: 'BLOCK' | 'SUSPEND' | 'WARN' | 'ACTIVE') => {
    if (action === 'ACTIVE') {
      onUnblockUser(user.id);
      setSelectedUserForDetail(prev => prev && prev.id === user.id ? { ...prev, status: 'ACTIVE' } : prev);
    } else if (action === 'WARN') {
      onWarnUser(user);
      setSelectedUserForDetail(prev => prev && prev.id === user.id ? { ...prev, status: 'WARNED', warningCount: prev.warningCount + 1 } : prev);
    } else if (action === 'BLOCK') {
      onOpenBlockModal(user);
    } else if (action === 'SUSPEND') {
      if (onSuspendUser) {
        onSuspendUser(user);
        setSelectedUserForDetail(prev => prev && prev.id === user.id ? { ...prev, status: 'SUSPENDED' } : prev);
      } else if (onDirectModerate) {
        onDirectModerate(user.id, 'SUSPENDED', 'Administrative temporary 7-day suspension for guideline review');
        setSelectedUserForDetail(prev => prev && prev.id === user.id ? { ...prev, status: 'SUSPENDED' } : prev);
      }
    }
  };

  return (
    <div>
      {/* Tab Navigation: Registration-based separation */}
      <div
        style={{
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'space-between',
          borderBottom: '2px solid #e4e4e7',
          marginBottom: '24px',
          paddingBottom: '2px'
        }}
      >
        <div style={{ display: 'flex', gap: '8px' }}>
          {/* Mentors Tab */}
          <button
            onClick={() => setActiveTab('MENTOR')}
            style={{
              padding: '12px 20px',
              border: 'none',
              background: 'none',
              cursor: 'pointer',
              fontWeight: 800,
              fontSize: '15px',
              color: activeTab === 'MENTOR' ? '#09090b' : '#71717a',
              borderBottom: activeTab === 'MENTOR' ? '3px solid #09090b' : '3px solid transparent',
              marginBottom: '-2px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              transition: 'all 0.15s ease'
            }}
          >
            <GraduationCap size={18} color={activeTab === 'MENTOR' ? '#09090b' : '#71717a'} />
            Mentors
            <span
              style={{
                fontSize: '11px',
                fontWeight: 800,
                padding: '2px 8px',
                borderRadius: '12px',
                backgroundColor: activeTab === 'MENTOR' ? '#09090b' : '#f4f4f5',
                color: activeTab === 'MENTOR' ? '#ffffff' : '#71717a'
              }}
            >
              {mentorCount}
            </span>
          </button>

          {/* Learners Tab */}
          <button
            onClick={() => setActiveTab('LEARNER')}
            style={{
              padding: '12px 20px',
              border: 'none',
              background: 'none',
              cursor: 'pointer',
              fontWeight: 800,
              fontSize: '15px',
              color: activeTab === 'LEARNER' ? '#09090b' : '#71717a',
              borderBottom: activeTab === 'LEARNER' ? '3px solid #09090b' : '3px solid transparent',
              marginBottom: '-2px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              transition: 'all 0.15s ease'
            }}
          >
            <UsersIcon size={18} color={activeTab === 'LEARNER' ? '#09090b' : '#71717a'} />
            Learners
            <span
              style={{
                fontSize: '11px',
                fontWeight: 800,
                padding: '2px 8px',
                borderRadius: '12px',
                backgroundColor: activeTab === 'LEARNER' ? '#09090b' : '#f4f4f5',
                color: activeTab === 'LEARNER' ? '#ffffff' : '#71717a'
              }}
            >
              {learnerCount}
            </span>
          </button>

          {/* All Users Tab */}
          <button
            onClick={() => setActiveTab('ALL')}
            style={{
              padding: '12px 20px',
              border: 'none',
              background: 'none',
              cursor: 'pointer',
              fontWeight: 800,
              fontSize: '15px',
              color: activeTab === 'ALL' ? '#09090b' : '#71717a',
              borderBottom: activeTab === 'ALL' ? '3px solid #09090b' : '3px solid transparent',
              marginBottom: '-2px',
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              transition: 'all 0.15s ease'
            }}
          >
            All Accounts
            <span
              style={{
                fontSize: '11px',
                fontWeight: 800,
                padding: '2px 8px',
                borderRadius: '12px',
                backgroundColor: activeTab === 'ALL' ? '#09090b' : '#f4f4f5',
                color: activeTab === 'ALL' ? '#ffffff' : '#71717a'
              }}
            >
              {users.length}
            </span>
          </button>
        </div>

        {/* Total Summary pill */}
        <div style={{ fontSize: '13px', color: '#71717a', fontWeight: 600 }}>
          Showing <strong style={{ color: '#09090b' }}>{filteredUsers.length}</strong> matching users
        </div>
      </div>

      {/* Search Bar & Status Filter */}
      <div className="glass-card" style={{ padding: '18px 24px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          {/* Dedicated Search Bar */}
          <div className="search-input-wrapper" style={{ flex: 1, minWidth: '320px', maxWidth: '520px' }}>
            <Search size={16} />
            <input
              type="text"
              className="search-input"
              placeholder={`Search ${activeTab === 'MENTOR' ? 'mentors' : activeTab === 'LEARNER' ? 'learners' : 'users'} by name, login ID, email, or city...`}
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            {search && (
              <button
                onClick={() => setSearch('')}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#71717a', padding: 0 }}
              >
                <X size={14} />
              </button>
            )}
          </div>

          {/* Status Filter */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <span style={{ fontSize: '12px', color: '#71717a', fontWeight: 700, textTransform: 'uppercase' }}>
              Status:
            </span>
            <select
              className="select-input"
              value={statusFilter}
              onChange={(e) => setStatusFilter(e.target.value as any)}
              style={{ fontWeight: 700 }}
            >
              <option value="ALL">All Statuses</option>
              <option value="ACTIVE">Active (Normal)</option>
              <option value="WARNED">Warned</option>
              <option value="BLOCKED">Blocked / Suspended</option>
            </select>
          </div>

          {/* Download / Export User Data Action */}
          <button
            type="button"
            id="download-user-data-btn"
            onClick={() => setIsExportModalOpen(true)}
            className="btn"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: '#09090b',
              color: '#ffffff',
              fontWeight: 800,
              fontSize: '13px',
              padding: '9px 18px',
              borderRadius: '8px',
              cursor: 'pointer',
              border: 'none',
              boxShadow: '0 2px 4px rgba(0,0,0,0.1)',
              transition: 'all 0.15s ease'
            }}
          >
            <Download size={16} color="#10b981" />
            Download User Data
          </button>
        </div>
      </div>

      {/* Users Data Table */}
      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <div className="table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>User / Profile</th>
                <th>Login ID</th>
                <th>Role & Type</th>
                <th>Financials / Activity</th>
                <th>Status & Safety</th>
                <th>Trust</th>
                <th style={{ textAlign: 'right' }}>Moderation & Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredUsers.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '48px', color: '#71717a', fontWeight: 600 }}>
                    No {activeTab.toLowerCase()} accounts found matching "{search}".
                  </td>
                </tr>
              ) : (
                filteredUsers.map(user => {
                  const isBlocked = user.status === 'BLOCKED' || user.status === 'SUSPENDED';
                  const isWarned = user.status === 'WARNED';
                  const isMentor = user.role === 'MENTOR';

                  return (
                    <tr
                      key={user.id}
                      style={{
                        background: isBlocked ? '#fef2f2' : undefined,
                        transition: 'background-color 0.15s ease'
                      }}
                    >
                      {/* User Profile Info - Clicking opens details or analytics */}
                      <td>
                        <div
                          style={{ display: 'flex', alignItems: 'center', gap: '12px', cursor: 'pointer' }}
                          onClick={() => {
                            if (isMentor) {
                              setSelectedMentorForAnalytics(user);
                            } else {
                              setSelectedUserForDetail(user);
                            }
                          }}
                          title={isMentor ? 'Click to view Growth Analytics' : 'Click to view user details'}
                        >
                          <div style={{ position: 'relative' }}>
                            <img
                              src={user.avatarUrl}
                              alt={user.name}
                              style={{
                                width: '44px',
                                height: '44px',
                                borderRadius: '50%',
                                objectFit: 'cover',
                                border: isBlocked ? '2px solid #ef4444' : '2px solid #e4e4e7'
                              }}
                            />
                            {isMentor && (
                              <div
                                style={{
                                  position: 'absolute',
                                  bottom: -2,
                                  right: -2,
                                  backgroundColor: '#10b981',
                                  color: '#ffffff',
                                  borderRadius: '50%',
                                  width: '16px',
                                  height: '16px',
                                  display: 'flex',
                                  alignItems: 'center',
                                  justifyContent: 'center',
                                  fontSize: '9px',
                                  border: '1.5px solid #ffffff'
                                }}
                                title="Mentor"
                              >
                                ★
                              </div>
                            )}
                          </div>
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                              <span style={{ fontWeight: 800, fontSize: '14px', color: '#09090b' }}>
                                {user.name}
                              </span>
                              {isMentor && (
                                <span
                                  style={{
                                    fontSize: '10px',
                                    fontWeight: 800,
                                    color: '#065f46',
                                    backgroundColor: '#ecfdf5',
                                    padding: '1px 6px',
                                    borderRadius: '10px'
                                  }}
                                >
                                  Growth Chart
                                </span>
                              )}
                            </div>
                            <p style={{ fontSize: '12px', color: '#71717a', margin: '2px 0 0', fontWeight: 500 }}>
                              {user.email}
                            </p>
                          </div>
                        </div>
                      </td>

                      {/* Login ID */}
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <KeyRound size={13} color="#71717a" />
                          <code style={{ fontSize: '12px', fontWeight: 700, color: '#09090b', backgroundColor: '#f4f4f5', padding: '2px 6px', borderRadius: '4px' }}>
                            {user.loginId || user.email.split('@')[0]}
                          </code>
                        </div>
                      </td>

                      {/* Role & Registration */}
                      <td>
                        <span
                          style={{
                            fontSize: '11px',
                            fontWeight: 800,
                            padding: '3px 8px',
                            borderRadius: '12px',
                            backgroundColor: isMentor ? '#09090b' : '#f4f4f5',
                            color: isMentor ? '#ffffff' : '#09090b'
                          }}
                        >
                          {user.role}
                        </span>
                        <div style={{ fontSize: '11px', color: '#71717a', marginTop: '3px' }}>
                          Joined {user.joinedDate}
                        </div>
                      </td>

                      {/* Financials / Activity */}
                      <td>
                        {isMentor ? (
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 800, color: '#10b981', fontSize: '14px' }}>
                              <DollarSign size={14} />
                              ${user.earnings?.totalEarned?.toFixed(2) || '0.00'}
                            </div>
                            <div style={{ fontSize: '11px', color: '#71717a', marginTop: '2px' }}>
                              {user.courseSales?.length || 0} sales • {user.uploadsCount} courses
                            </div>
                          </div>
                        ) : (
                          <div>
                            <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontWeight: 700, color: '#09090b', fontSize: '13px' }}>
                              <ShoppingBag size={14} color="#71717a" />
                              {user.purchasedCourses?.length || 0} Courses Bought
                            </div>
                            <div style={{ fontSize: '11px', color: '#71717a', marginTop: '2px' }}>
                              Active learner
                            </div>
                          </div>
                        )}
                      </td>

                      {/* Status & Safety Flag */}
                      <td>
                        <span
                          style={{
                            fontSize: '11px',
                            fontWeight: 800,
                            padding: '3px 8px',
                            borderRadius: '12px',
                            backgroundColor: isBlocked ? '#fef2f2' : isWarned ? '#fffbeb' : '#ecfdf5',
                            color: isBlocked ? '#ef4444' : isWarned ? '#b45309' : '#10b981',
                            border: `1px solid ${isBlocked ? '#fecaca' : isWarned ? '#fde68a' : '#a7f3d0'}`
                          }}
                        >
                          {user.status}
                        </span>
                        {user.warningCount > 0 && (
                          <div style={{ fontSize: '11px', color: '#b45309', fontWeight: 700, marginTop: '3px' }}>
                            ⚠ {user.warningCount} {user.warningCount === 1 ? 'warning' : 'warnings'}
                          </div>
                        )}
                      </td>

                      {/* Trust Score */}
                      <td>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                          <div
                            style={{
                              width: '46px',
                              height: '6px',
                              borderRadius: '3px',
                              background: '#e4e4e7',
                              overflow: 'hidden'
                            }}
                          >
                            <div
                              style={{
                                height: '100%',
                                width: `${user.trustScore}%`,
                                backgroundColor: user.trustScore > 80 ? '#10b981' : user.trustScore > 50 ? '#f59e0b' : '#ef4444'
                              }}
                            />
                          </div>
                          <span style={{ fontSize: '12px', fontWeight: 800, color: '#09090b' }}>
                            {user.trustScore}%
                          </span>
                        </div>
                      </td>

                      {/* Actions: Details, Analytics, Warn, Suspend, Block */}
                      <td style={{ textAlign: 'right' }}>
                        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', flexWrap: 'wrap', justifyContent: 'flex-end' }}>
                          {/* Details button */}
                          <button
                            className="btn btn-outline btn-sm"
                            title="View Full Personal Details, Login Credentials & Purchases"
                            onClick={() => setSelectedUserForDetail(user)}
                            style={{ padding: '5px 9px', fontSize: '11px', fontWeight: 700 }}
                          >
                            <Eye size={12} />
                            Details
                          </button>

                          {/* User Growth Analytics button */}
                          <button
                            onClick={() => setSelectedMentorForAnalytics(user)}
                            title="Open Full Screen Growth Analytics"
                            style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '4px',
                              backgroundColor: '#ecfdf5',
                              color: '#065f46',
                              border: '1px solid #a7f3d0',
                              padding: '5px 9px',
                              borderRadius: '6px',
                              fontSize: '11px',
                              fontWeight: 800,
                              cursor: 'pointer'
                            }}
                          >
                            <TrendingUp size={12} color="#10b981" />
                            Growth
                          </button>

                          {/* Warning button */}
                          <button
                            onClick={() => onWarnUser(user)}
                            title="Issue Warning for Bad Practice"
                            style={{
                              display: 'inline-flex',
                              alignItems: 'center',
                              gap: '4px',
                              backgroundColor: '#fffbeb',
                              color: '#b45309',
                              border: '1px solid #fde68a',
                              padding: '5px 8px',
                              borderRadius: '6px',
                              fontSize: '11px',
                              fontWeight: 700,
                              cursor: 'pointer'
                            }}
                          >
                            <AlertTriangle size={12} />
                            Warn
                          </button>

                          {/* Suspend button */}
                          {(onSuspendUser || onDirectModerate) && (
                            <button
                              onClick={() => {
                                if (onSuspendUser) onSuspendUser(user);
                                else if (onDirectModerate) onDirectModerate(user.id, 'SUSPENDED', '7-day policy suspension');
                              }}
                              title="Temporarily Suspend Account"
                              style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                backgroundColor: '#fff7ed',
                                color: '#c2410c',
                                border: '1px solid #fed7aa',
                                padding: '5px 8px',
                                borderRadius: '6px',
                                fontSize: '11px',
                                fontWeight: 700,
                                cursor: 'pointer'
                              }}
                            >
                              <Clock size={12} />
                              Suspend
                            </button>
                          )}

                          {/* Block / Unblock button */}
                          {isBlocked ? (
                            <button
                              onClick={() => onUnblockUser(user.id)}
                              title="Unblock User Access"
                              style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                backgroundColor: '#10b981',
                                color: '#ffffff',
                                border: 'none',
                                padding: '5px 10px',
                                borderRadius: '6px',
                                fontSize: '11px',
                                fontWeight: 800,
                                cursor: 'pointer'
                              }}
                            >
                              <CheckCircle size={12} />
                              Unblock
                            </button>
                          ) : (
                            <button
                              onClick={() => onOpenBlockModal(user)}
                              title="Block User from Platform"
                              style={{
                                display: 'inline-flex',
                                alignItems: 'center',
                                gap: '4px',
                                backgroundColor: '#ef4444',
                                color: '#ffffff',
                                border: 'none',
                                padding: '5px 10px',
                                borderRadius: '6px',
                                fontSize: '11px',
                                fontWeight: 800,
                                cursor: 'pointer'
                              }}
                            >
                              <Ban size={12} />
                              Block
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

      {/* User Details Modal (Personal info, credentials, eye password toggle, earnings, purchases, moderation) */}
      <UserDetailsModal
        user={selectedUserForDetail}
        isOpen={!!selectedUserForDetail}
        onClose={() => setSelectedUserForDetail(null)}
        onOpenAnalytics={(mentor) => setSelectedMentorForAnalytics(mentor)}
        onModerateUser={handleModerateFromModal}
      />

      {/* Mentor Growth Analytics Drawer (Slide-over graph on mentor profile click) */}
      <MentorAnalyticsDrawer
        mentor={selectedMentorForAnalytics}
        isOpen={!!selectedMentorForAnalytics}
        onClose={() => setSelectedMentorForAnalytics(null)}
      />

      {/* Download User Data Filtered by Time Periods Modal */}
      <ExportUserModal
        isOpen={isExportModalOpen}
        onClose={() => setIsExportModalOpen(false)}
        users={users}
      />
    </div>
  );
};
