import React, { useState, useEffect } from 'react';
import { AdminSidebar } from './components/layout/AdminSidebar';
import { AdminHeader } from './components/layout/AdminHeader';
import { BlockUserModal } from './components/users/BlockUserModal';
import { VideoPreviewModal } from './components/common/VideoPreviewModal';

import { DashboardOverview } from './pages/DashboardOverview';
import { UserManagement } from './pages/UserManagement';
import { UploadedContent } from './pages/UploadedContent';
import { ModerationReports } from './pages/ModerationReports';
import { AdminAuditLogs } from './pages/AdminAuditLogs';

import { AdminApiService } from './services/api';
import { AdminUser, UploadedVideo, UploadedCourse, ModerationReport, AuditLogEntry, PlatformStats, UserStatus } from './types/admin';
import { CheckCircle, AlertOctagon } from 'lucide-react';

export const App: React.FC = () => {
  const [currentTab, setCurrentTab] = useState<string>('overview');
  const [searchQuery, setSearchQuery] = useState<string>('');

  // Data State
  const [users, setUsers] = useState<AdminUser[]>([]);
  const [courses, setCourses] = useState<UploadedCourse[]>([]);
  const [videos, setVideos] = useState<UploadedVideo[]>([]);
  const [reports, setReports] = useState<ModerationReport[]>([]);
  const [logs, setLogs] = useState<AuditLogEntry[]>([]);
  const [stats, setStats] = useState<PlatformStats>({
    totalUsers: 0,
    totalMentors: 0,
    totalLearners: 0,
    activeUsersToday: 0,
    blockedUsersCount: 0,
    warnedUsersCount: 0,
    totalCoursesUploaded: 0,
    totalVideosUploaded: 0,
    totalHoursContent: 0,
    pendingReportsCount: 0
  });

  // Modals state
  const [userToBlock, setUserToBlock] = useState<AdminUser | null>(null);
  const [videoToInspect, setVideoToInspect] = useState<UploadedVideo | null>(null);
  const [toastMessage, setToastMessage] = useState<{ text: string; type: 'SUCCESS' | 'DANGER' } | null>(null);

  const showToast = (text: string, type: 'SUCCESS' | 'DANGER' = 'SUCCESS') => {
    setToastMessage({ text, type });
    setTimeout(() => setToastMessage(null), 4000);
  };

  const reloadData = () => {
    setUsers(AdminApiService.getUsers());
    setCourses(AdminApiService.getCourses());
    setVideos(AdminApiService.getVideos());
    setReports(AdminApiService.getReports());
    setLogs(AdminApiService.getAuditLogs());
    setStats(AdminApiService.getStats());
  };

  useEffect(() => {
    reloadData();
  }, []);

  // Action: Block / Suspend / Warn User
  const handleBlockConfirm = (userId: string, newStatus: UserStatus, reason: string) => {
    const res = AdminApiService.updateUserStatus(userId, newStatus, reason);
    if (res.success && res.user) {
      reloadData();
      showToast(
        newStatus === 'BLOCKED'
          ? `User ${res.user.name} has been BLOCKED from accessing their account.`
          : newStatus === 'SUSPENDED'
          ? `User ${res.user.name} has been SUSPENDED.`
          : `Warning issued to ${res.user.name}.`,
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

  // Action: Flag / Remove / Approve Video
  const handleFlagVideo = (video: UploadedVideo) => {
    AdminApiService.updateVideoStatus(video.id, 'FLAGGED', 'Marked as bad practice / policy review by admin.');
    reloadData();
    showToast(`Video "${video.title}" flagged for bad practice review.`, 'DANGER');
  };

  const handleRemoveVideo = (video: UploadedVideo) => {
    AdminApiService.updateVideoStatus(video.id, 'REMOVED', 'Removed from server due to bad practice policy violation.');
    reloadData();
    showToast(`Video "${video.title}" removed from server.`, 'DANGER');
  };

  const handleApproveVideo = (video: UploadedVideo) => {
    AdminApiService.updateVideoStatus(video.id, 'APPROVED');
    reloadData();
    showToast(`Video "${video.title}" approved for live streaming.`, 'SUCCESS');
  };

  // Action: Resolve / Dismiss Report
  const handleResolveReport = (reportId: string, note: string) => {
    AdminApiService.resolveReport(reportId, note);
    reloadData();
    showToast('Report marked as resolved.', 'SUCCESS');
  };

  const handleDismissReport = (reportId: string) => {
    AdminApiService.dismissReport(reportId);
    reloadData();
    showToast('Report dismissed.', 'SUCCESS');
  };

  // Reset Demo
  const handleResetData = () => {
    AdminApiService.resetDemoData();
    reloadData();
    showToast('Demo data reset to clean initial state.', 'SUCCESS');
  };

  const getHeaderTitle = () => {
    switch (currentTab) {
      case 'users': return 'User Governance & Access Control';
      case 'uploads': return 'Uploaded Courses & Video Materials';
      case 'reports': return 'Community Reports & Bad Practice Incidents';
      case 'audit': return 'Administrative Audit Trail';
      default: return 'SkillBuilder Administration Dashboard';
    }
  };

  const getHeaderSubtitle = () => {
    switch (currentTab) {
      case 'users': return 'Inspect accounts, detect bad practice, block offenders, or restore access';
      case 'uploads': return 'Review mentor video lectures, curriculum standards, and media uploads';
      case 'reports': return 'Moderate reports filed by learners and automated compliance triggers';
      case 'audit': return 'Comprehensive history of actions taken by platform moderators';
      default: return 'Real-time overview of users, uploads, and security operations';
    }
  };

  return (
    <div className="app-container">
      {/* Toast Banner */}
      {toastMessage && (
        <div style={{
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
        }}>
          {toastMessage.type === 'DANGER' ? <AlertOctagon size={18} /> : <CheckCircle size={18} />}
          <span>{toastMessage.text}</span>
        </div>
      )}

      {/* Sidebar */}
      <AdminSidebar
        currentTab={currentTab}
        onSelectTab={setCurrentTab}
        pendingReportsCount={stats.pendingReportsCount}
        blockedUsersCount={stats.blockedUsersCount}
        onResetData={handleResetData}
      />

      {/* Main Content Area */}
      <div className="main-content">
        <AdminHeader
          title={getHeaderTitle()}
          subtitle={getHeaderSubtitle()}
          searchQuery={searchQuery}
          onSearchChange={currentTab === 'users' || currentTab === 'uploads' ? setSearchQuery : undefined}
          pendingReportsCount={stats.pendingReportsCount}
        />

        <main className="page-container">
          {currentTab === 'overview' && (
            <DashboardOverview
              stats={stats}
              users={users}
              videos={videos}
              reports={reports}
              onNavigate={setCurrentTab}
              onOpenBlockModal={setUserToBlock}
              onInspectVideo={setVideoToInspect}
            />
          )}

          {currentTab === 'users' && (
            <UserManagement
              users={users}
              onOpenBlockModal={setUserToBlock}
              onUnblockUser={handleUnblockUser}
              onWarnUser={(user) => handleBlockConfirm(user.id, 'WARNED', 'Official administrative warning issued.')}
            />
          )}

          {currentTab === 'uploads' && (
            <UploadedContent
              courses={courses}
              videos={videos}
              onInspectVideo={setVideoToInspect}
              onFlagVideo={handleFlagVideo}
              onRemoveVideo={handleRemoveVideo}
              onApproveVideo={handleApproveVideo}
            />
          )}

          {currentTab === 'reports' && (
            <ModerationReports
              reports={reports}
              users={users}
              onResolveReport={handleResolveReport}
              onDismissReport={handleDismissReport}
              onOpenBlockModal={setUserToBlock}
            />
          )}

          {currentTab === 'audit' && (
            <AdminAuditLogs logs={logs} />
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

      {/* Video Inspection Preview Modal */}
      <VideoPreviewModal
        video={videoToInspect}
        isOpen={!!videoToInspect}
        onClose={() => setVideoToInspect(null)}
        onFlag={handleFlagVideo}
        onRemove={handleRemoveVideo}
        onApprove={handleApproveVideo}
      />
    </div>
  );
};

export default App;
