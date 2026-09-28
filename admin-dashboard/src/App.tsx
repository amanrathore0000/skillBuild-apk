import React, { useState, useEffect } from 'react';
import { AdminSidebar } from './components/layout/AdminSidebar';
import { AdminHeader } from './components/layout/AdminHeader';
import { BlockUserModal } from './components/users/BlockUserModal';
import { UserManagement } from './pages/UserManagement';
import { HelpAndSupport } from './pages/HelpAndSupport';
import { AdminLogin } from './pages/AdminLogin';
import { AdminApiService, AdminAuthService } from './services/api';
import { AdminUser, SupportComplaint, ComplaintStatus, UserStatus } from './types/admin';
import { CheckCircle, AlertOctagon } from 'lucide-react';

export const App: React.FC = () => {
  // Authentication State
  const [isAuthenticated, setIsAuthenticated] = useState<boolean>(() => AdminAuthService.isAuthenticated());

  const [currentTab, setCurrentTab] = useState<string>('users');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Data State
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [complaints, setComplaints] = useState<SupportComplaint[]>([]);

  // Modals state
  const [userToBlock, setUserToBlock] = useState<AdminUser | null>(null);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'SUCCESS' | 'DANGER' } | null>(null);

  const showToast = (text: string, type: 'SUCCESS' | 'DANGER' = 'SUCCESS') => {
    setToastMessage({ text, type });
    setTimeout(() => setToastMessage(null), 4000);
  };

  const reloadData = () => {
    setUsers(AdminApiService.getUsers());
    setComplaints(AdminApiService.getComplaints());
  };

  useEffect(() => {
    if (isAuthenticated) {
      reloadData();
    }
  }, [isAuthenticated]);

  // Action: Logout
  const handleLogout = () => {
    AdminAuthService.logout();
    setIsAuthenticated(false);
    showToast('Administrator session closed successfully.', 'SUCCESS');
  };

  // Action: Block / Suspend / Warn User
  const handleBlockConfirm = (userId: string, newStatus: UserStatus, reason: string) => {
    const res = AdminApiService.updateUserStatus(userId, newStatus, reason);
    if (res.success && res.user) {
      reloadData();
      showToast(
        newStatus === 'BLOCKED'
          ? `User ${res.user.name} has been BLOCKED from platform access.`
          : newStatus === 'SUSPENDED'
          ? `User ${res.user.name} has been SUSPENDED.`
          : `Formal warning issued to ${res.user.name}.`,
        newStatus === 'BLOCKED' ? 'DANGER' : 'SUCCESS'
      );
    }
  };

  // Action: Unblock User
  const handleUnblockUser = (userId: string) => {
    const res = AdminApiService.updateUserStatus(userId, 'ACTIVE', 'Access restored by administrator review.');
    if (res.success && res.user) {
      reloadData();
      showToast(`Account access restored for ${res.user.name}.`, 'SUCCESS');
    }
  };

  // Action: Update Complaint Status
  const handleUpdateComplaintStatus = (id: string, status: ComplaintStatus, resolutionNote?: string) => {
    const res = AdminApiService.updateComplaintStatus(id, status, resolutionNote);
    if (res.success) {
      reloadData();
      showToast(`Support Ticket #${id} marked as ${status}.`, 'SUCCESS');
    }
  };

  // Reset Demo Data
  const handleResetData = () => {
    AdminApiService.resetDemoData();
    reloadData();
    showToast('All moderation and complaints data reset to default.', 'SUCCESS');
  };

  // If not authenticated, force the Admin Login Gate
  if (!isAuthenticated) {
    return (
      <AdminLogin
        onLoginSuccess={(session) => {
          setIsAuthenticated(true);
          showToast(`Welcome back, ${session.name}! Access unlocked.`, 'SUCCESS');
        }}
      />
    );
  }

  const blockedUsersCount = users.filter(u => u.status === 'BLOCKED' || u.status === 'SUSPENDED').length;
  const pendingComplaintsCount = complaints.filter(c => c.status === 'PENDING').length;

  const getHeaderTitle = () => {
    switch (currentTab) {
      case 'support':
        return 'Help & Support Portal — User Complaints';
      case 'users':
      default:
        return 'User Governance, Credentials & Earnings Directory';
    }
  };

  const getHeaderSubtitle = () => {
    switch (currentTab) {
      case 'support':
        return 'Review user complaints reported through the mobile app, investigate disputes, and take resolution actions';
      case 'users':
      default:
        return 'Inspect learner & mentor personal details, credentials, revenue growth graphs, and enforce moderation controls';
    }
  };

  return (
    <div className="app-container">
      {/* Toast Banner */}
      {toastMessage && (
        <div
          style={{
            position: 'fixed',
            top: '24px',
            right: '32px',
            zIndex: 9999,
            padding: '14px 20px',
            borderRadius: '12px',
            background: toastMessage.type === 'DANGER' ? 'linear-gradient(135deg, #ef4444, #dc2626)' : 'linear-gradient(135deg, #10b981, #059669)',
            color: '#fff',
            boxShadow: '0 10px 25px rgba(0, 0, 0, 0.4)',
            display: 'flex',
            alignItems: 'center',
            gap: '10px',
            fontSize: '14px',
            fontWeight: 700,
            animation: 'fadeIn 0.2s ease-out'
          }}
        >
          {toastMessage.type === 'DANGER' ? <AlertOctagon size={18} /> : <CheckCircle size={18} />}
          <span>{toastMessage.text}</span>
        </div>
      )}

      {/* Sidebar - Users and Help & Support only */}
      <AdminSidebar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        pendingComplaintsCount={pendingComplaintsCount}
        blockedUsersCount={blockedUsersCount}
        onResetData={handleResetData}
        onLogout={handleLogout}
      />

      {/* Main Content Area */}
      <div className="main-content">
        <AdminHeader
          title={getHeaderTitle()}
          subtitle={getHeaderSubtitle()}
          searchQuery={searchQuery}
          onSearchChange={undefined}
          pendingReportsCount={pendingComplaintsCount}
          onLogout={handleLogout}
        />

        <main className="page-container">
          {currentTab === 'users' && (
            <UserManagement
              users={users}
              onOpenBlockModal={setUserToBlock}
              onUnblockUser={handleUnblockUser}
              onWarnUser={(user) => handleBlockConfirm(user.id, 'WARNED', 'Official administrative warning issued.')}
              onDirectModerate={(id, status, reason) => handleBlockConfirm(id, status, reason)}
            />
          )}

          {currentTab === 'support' && (
            <HelpAndSupport
              complaints={complaints}
              users={users}
              onUpdateComplaintStatus={handleUpdateComplaintStatus}
              onOpenBlockModal={setUserToBlock}
            />
          )}
        </main>
      </div>

      {/* Block/Suspend User Modal */}
      <BlockUserModal
        user={userToBlock}
        isOpen={!!userToBlock}
        onClose={() => setUserToBlock(null)}
        onConfirm={handleBlockConfirm}
      />
    </div>
  );
};

export default App;
