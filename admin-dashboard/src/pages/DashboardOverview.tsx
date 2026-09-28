import React from 'react';
import {
  Users,
  UserCheck,
  Video,
  ShieldAlert,
  Ban,
  ArrowUpRight,
  AlertTriangle,
  Play,
  CheckCircle2
} from 'lucide-react';
import { MetricCard } from '../components/layout/MetricCard';
import { PlatformStats, AdminUser, UploadedVideo, ModerationReport } from '../types/admin';

interface OverviewProps {
  stats: PlatformStats;
  users: AdminUser[];
  videos: UploadedVideo[];
  reports: ModerationReport[];
  onNavigate: (tab: string) => void;
  onOpenBlockModal: (user: AdminUser) => void;
  onInspectVideo: (video: UploadedVideo) => void;
}

export const DashboardOverview: React.FC<OverviewProps> = ({
  stats,
  users,
  videos,
  reports,
  onNavigate,
  onOpenBlockModal,
  onInspectVideo
}) => {
  const pendingReports = reports.filter(r => r.status === 'PENDING').slice(0, 3);
  const flaggedOrRecentVideos = videos.slice(0, 4);

  return (
    <div>
      {/* Welcome Banner with Safety Status */}
      <div className="glass-card" style={{
        padding: '24px 28px',
        marginBottom: '28px',
        background: 'linear-gradient(135deg, #ecfdf5 0%, #ffffff 60%)',
        border: '1.5px solid #a7f3d0',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '20px',
        boxShadow: '0 4px 20px rgba(16, 185, 129, 0.08)'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span className="badge badge-active">Platform System Healthy</span>
            <span style={{ fontSize: '12px', color: '#64748b', fontWeight: 600 }}>Real-time Safety Telemetry</span>
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, marginTop: '8px', letterSpacing: '-0.4px', color: '#09090b' }}>
            SkillBuilder Safety & Governance Command Center
          </h2>
          <p style={{ fontSize: '14px', color: '#334155', marginTop: '4px', maxWidth: '680px', fontWeight: 500 }}>
            Monitor user growth, inspect video courses uploaded by mentors in real-time, enforce platform guidelines, and suspend accounts engaged in bad practice.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <button
            className="btn"
            style={{
              background: '#fef2f2',
              color: '#b91c1c',
              border: '1.5px solid #fecaca',
              boxShadow: '0 2px 8px rgba(239, 68, 68, 0.1)'
            }}
            onClick={() => onNavigate('reports')}
          >
            <ShieldAlert size={16} color="#ef4444" />
            View Reports ({stats.pendingReportsCount})
          </button>
          <button className="btn btn-primary" onClick={() => onNavigate('users')}>
            <Users size={16} />
            Manage Users
          </button>
        </div>
      </div>

      {/* KPI Metrics Grid */}
      <div className="grid-stats">
        <MetricCard
          title="Total Users"
          value={stats.totalUsers}
          subtitle="Learners & Mentors combined"
          change="+14% this month"
          icon={Users}
          gradient="linear-gradient(135deg, #10b981, #059669)"
          glowColor="rgba(16, 185, 129, 0.25)"
        />
        <MetricCard
          title="Verified Mentors"
          value={stats.totalMentors}
          subtitle={`${stats.totalLearners} Active Learners`}
          change={`${Math.round((stats.totalMentors / (stats.totalUsers || 1)) * 100)}% mentor ratio`}
          icon={UserCheck}
          gradient="linear-gradient(135deg, #10b981, #047857)"
          glowColor="rgba(16, 185, 129, 0.25)"
        />
        <MetricCard
          title="Uploaded Videos"
          value={stats.totalVideosUploaded}
          subtitle={`${stats.totalCoursesUploaded} Courses on server`}
          change="+6 uploaded today"
          icon={Video}
          gradient="linear-gradient(135deg, #10b981, #059669)"
          glowColor="rgba(16, 185, 129, 0.25)"
        />
        <MetricCard
          title="Blocked / Suspended"
          value={stats.blockedUsersCount}
          subtitle={`${stats.warnedUsersCount} Accounts with warnings`}
          change={stats.blockedUsersCount > 0 ? "Violations Enforced" : "All Clean"}
          isPositive={stats.blockedUsersCount === 0}
          isDangerCard={stats.blockedUsersCount > 0}
          icon={Ban}
          gradient="linear-gradient(135deg, #ef4444, #dc2626)"
          glowColor="rgba(239, 68, 68, 0.3)"
        />
      </div>

      {/* Main 2-Column Section */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(460px, 1fr))', gap: '24px' }}>
        {/* Left Column: Recent Bad Practice Reports (RED ACCENT) */}
        <div className="glass-card" style={{ padding: '24px', borderTop: '4px solid #ef4444' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '10px',
                background: '#fef2f2',
                border: '1px solid #fecaca',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ef4444'
              }}>
                <AlertTriangle size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: 800, color: '#09090b' }}>Bad Practice Incident Queue</h3>
                <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>Flagged community reports pending admin decision</p>
              </div>
            </div>
            <button
              onClick={() => onNavigate('reports')}
              className="btn btn-outline btn-sm"
              style={{ display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              All Reports <ArrowUpRight size={13} />
            </button>
          </div>

          {pendingReports.length === 0 ? (
            <div style={{ padding: '36px', textAlign: 'center', color: '#64748b' }}>
              <CheckCircle2 size={38} color="#10b981" style={{ margin: '0 auto 10px' }} />
              <p style={{ fontWeight: 700, color: '#09090b' }}>All clean! No pending bad practice reports.</p>
            </div>
          ) : (
            <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
              {pendingReports.map(report => {
                const targetUser = users.find(u => u.id === report.reportedUserId);
                return (
                  <div
                    key={report.id}
                    style={{
                      padding: '14px 16px',
                      borderRadius: '10px',
                      background: '#ffffff',
                      border: '1px solid #fecaca',
                      borderLeft: '4px solid #ef4444',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: '12px',
                      boxShadow: '0 2px 6px rgba(239, 68, 68, 0.04)'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', minWidth: 0 }}>
                      <img
                        src={report.reportedUserAvatar}
                        alt={report.reportedUserName}
                        style={{ width: '40px', height: '40px', borderRadius: '50%', objectFit: 'cover', border: '1.5px solid #fecaca' }}
                      />
                      <div style={{ minWidth: 0 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ fontWeight: 800, fontSize: '13px', color: '#09090b' }}>{report.reportedUserName}</span>
                          <span className="badge badge-blocked" style={{ fontSize: '9px', padding: '2px 6px' }}>
                            {report.severity}
                          </span>
                        </div>
                        <p style={{ fontSize: '12px', color: '#475569', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap', fontWeight: 500 }}>
                          {report.description}
                        </p>
                      </div>
                    </div>

                    <div style={{ display: 'flex', gap: '6px', flexShrink: 0 }}>
                      {targetUser && targetUser.status !== 'BLOCKED' && (
                        <button
                          className="btn btn-danger btn-sm"
                          onClick={() => onOpenBlockModal(targetUser)}
                        >
                          <Ban size={13} />
                          Block
                        </button>
                      )}
                    </div>
                  </div>
                );
              })}
            </div>
          )}
        </div>

        {/* Right Column: User Uploads Review Feed (GREEN ACCENT) */}
        <div className="glass-card" style={{ padding: '24px', borderTop: '4px solid #10b981' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '20px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{
                width: '36px',
                height: '36px',
                borderRadius: '10px',
                background: '#ecfdf5',
                border: '1px solid #a7f3d0',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#10b981'
              }}>
                <Video size={20} />
              </div>
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: 800, color: '#09090b' }}>Recent Uploaded Media</h3>
                <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>Uploaded video lectures & trials across categories</p>
              </div>
            </div>
            <button
              onClick={() => onNavigate('uploads')}
              className="btn btn-outline btn-sm"
              style={{ display: 'flex', alignItems: 'center', gap: '4px' }}
            >
              All Uploads <ArrowUpRight size={13} />
            </button>
          </div>

          <div style={{ display: 'flex', flexDirection: 'column', gap: '12px' }}>
            {flaggedOrRecentVideos.map(video => (
              <div
                key={video.id}
                style={{
                  padding: '12px 14px',
                  borderRadius: '10px',
                  background: video.flaggedForBadPractice ? '#fef2f2' : '#ffffff',
                  border: `1.5px solid ${video.flaggedForBadPractice ? '#fecaca' : '#e2e8f0'}`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: '12px',
                  boxShadow: '0 1px 4px rgba(0, 0, 0, 0.03)'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', minWidth: 0 }}>
                  <div style={{
                    position: 'relative',
                    width: '64px',
                    height: '42px',
                    borderRadius: '8px',
                    overflow: 'hidden',
                    flexShrink: 0,
                    border: '1px solid #e2e8f0'
                  }}>
                    <img src={video.thumbnailUrl} alt={video.title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    <span style={{
                      position: 'absolute',
                      bottom: '2px',
                      right: '2px',
                      background: 'rgba(9, 9, 11, 0.85)',
                      fontSize: '9px',
                      fontWeight: 700,
                      padding: '1px 4px',
                      borderRadius: '3px',
                      color: '#ffffff'
                    }}>
                      {video.duration}
                    </span>
                  </div>

                  <div style={{ minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ fontWeight: 800, fontSize: '13px', color: '#09090b', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {video.title}
                      </span>
                      {video.flaggedForBadPractice ? (
                        <span className="badge badge-blocked" style={{ fontSize: '9px', padding: '1px 6px' }}>
                          FLAGGED
                        </span>
                      ) : (
                        <span className="badge badge-active" style={{ fontSize: '9px', padding: '1px 6px' }}>
                          APPROVED
                        </span>
                      )}
                    </div>
                    <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>
                      By {video.mentorName} • {video.category}
                    </p>
                  </div>
                </div>

                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => onInspectVideo(video)}
                >
                  <Play size={13} color="#09090b" />
                  Inspect
                </button>
              </div>
            ))}
          </div>
        </div>
      </div>
    </div>
  );
};
