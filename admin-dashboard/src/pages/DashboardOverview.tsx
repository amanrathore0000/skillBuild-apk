import React from 'react';
import {
  Users,
  UserCheck,
  Video,
  ShieldAlert,
  Ban,
  Clock,
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
        background: 'linear-gradient(135deg, rgba(99, 102, 241, 0.12), rgba(16, 185, 129, 0.05))',
        border: '1px solid rgba(99, 102, 241, 0.25)',
        display: 'flex',
        alignItems: 'center',
        justifyContent: 'space-between',
        flexWrap: 'wrap',
        gap: '20px'
      }}>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            <span className="badge badge-active">Platform System Healthy</span>
            <span style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Real-time Safety Telemetry</span>
          </div>
          <h2 style={{ fontSize: '24px', fontWeight: 800, marginTop: '8px', letterSpacing: '-0.3px' }}>
            SkillBuilder Safety & Governance Command Center
          </h2>
          <p style={{ fontSize: '14px', color: 'var(--text-secondary)', marginTop: '4px', maxWidth: '680px' }}>
            Monitor user growth, inspect video courses uploaded by mentors in real-time, enforce platform guidelines, and suspend accounts engaged in bad practice.
          </p>
        </div>

        <div style={{ display: 'flex', gap: '12px' }}>
          <button className="btn btn-outline" onClick={() => onNavigate('reports')}>
            <ShieldAlert size={16} color="var(--warning)" />
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
          gradient="linear-gradient(135deg, #6366f1, #4f46e5)"
        />
        <MetricCard
          title="Verified Mentors"
          value={stats.totalMentors}
          subtitle={`${stats.totalLearners} Active Learners`}
          change={`${Math.round((stats.totalMentors / stats.totalUsers) * 100)}% mentor ratio`}
          icon={UserCheck}
          gradient="linear-gradient(135deg, #0ea5e9, #0284c7)"
        />
        <MetricCard
          title="Uploaded Videos"
          value={stats.totalVideosUploaded}
          subtitle={`${stats.totalCoursesUploaded} Courses on server`}
          change="+6 uploaded today"
          icon={Video}
          gradient="linear-gradient(135deg, #10b981, #059669)"
        />
        <MetricCard
          title="Blocked / Suspended"
          value={stats.blockedUsersCount}
          subtitle={`${stats.warnedUsersCount} Accounts with warnings`}
          change={stats.blockedUsersCount > 0 ? "Action enforced" : "Clean"}
          isPositive={stats.blockedUsersCount === 0}
          icon={Ban}
          gradient="linear-gradient(135deg, #ef4444, #dc2626)"
          glowColor="rgba(239, 68, 68, 0.3)"
        />
      </div>

      {/* Main 2-Column Section */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(460px, 1fr))', gap: '24px' }}>
        {/* Left Column: Recent Bad Practice Reports */}
        <div className="glass-card" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '18px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: '8px',
                background: 'rgba(245, 158, 11, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--warning)'
              }}>
                <AlertTriangle size={18} />
              </div>
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: 700 }}>Bad Practice Incident Queue</h3>
                <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Flagged community reports pending admin decision</p>
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
            <div style={{ padding: '32px', textAlign: 'center', color: 'var(--text-muted)' }}>
              <CheckCircle2 size={36} color="var(--success)" style={{ margin: '0 auto 10px' }} />
              <p style={{ fontWeight: 600 }}>All clean! No pending bad practice reports.</p>
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
                      background: 'rgba(0, 0, 0, 0.25)',
                      border: '1px solid var(--border-subtle)',
                      display: 'flex',
                      alignItems: 'center',
                      justifyContent: 'space-between',
                      gap: '12px'
                    }}
                  >
                    <div style={{ display: 'flex', alignItems: 'center', gap: '12px', minWidth: 0 }}>
                      <img
                        src={report.reportedUserAvatar}
                        alt={report.reportedUserName}
                        style={{ width: '38px', height: '38px', borderRadius: '50%', objectFit: 'cover' }}
                      />
                      <div style={{ minWidth: 0 }}>
                        <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                          <span style={{ fontWeight: 700, fontSize: '13px' }}>{report.reportedUserName}</span>
                          <span className={`badge badge-${report.severity === 'CRITICAL' || report.severity === 'HIGH' ? 'blocked' : 'warned'}`} style={{ fontSize: '9px', padding: '1px 5px' }}>
                            {report.severity}
                          </span>
                        </div>
                        <p style={{ fontSize: '12px', color: 'var(--text-muted)', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
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

        {/* Right Column: User Uploads Review Feed */}
        <div className="glass-card" style={{ padding: '24px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '18px' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <div style={{
                width: '32px',
                height: '32px',
                borderRadius: '8px',
                background: 'rgba(99, 102, 241, 0.15)',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: 'var(--primary)'
              }}>
                <Video size={18} />
              </div>
              <div>
                <h3 style={{ fontSize: '16px', fontWeight: 700 }}>Recent Uploaded Media</h3>
                <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Uploaded video lectures & trials across categories</p>
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
                  background: video.flaggedForBadPractice ? 'rgba(239, 68, 68, 0.08)' : 'rgba(0, 0, 0, 0.25)',
                  border: `1px solid ${video.flaggedForBadPractice ? 'rgba(239, 68, 68, 0.3)' : 'var(--border-subtle)'}`,
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  gap: '12px'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px', minWidth: 0 }}>
                  <div style={{
                    position: 'relative',
                    width: '64px',
                    height: '40px',
                    borderRadius: '6px',
                    overflow: 'hidden',
                    flexShrink: 0
                  }}>
                    <img src={video.thumbnailUrl} alt={video.title} style={{ width: '100%', height: '100%', objectFit: 'cover' }} />
                    <span style={{
                      position: 'absolute',
                      bottom: '2px',
                      right: '2px',
                      background: 'rgba(0,0,0,0.8)',
                      fontSize: '8px',
                      padding: '1px 3px',
                      borderRadius: '2px',
                      color: '#fff'
                    }}>
                      {video.duration}
                    </span>
                  </div>

                  <div style={{ minWidth: 0 }}>
                    <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
                      <span style={{ fontWeight: 700, fontSize: '13px', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
                        {video.title}
                      </span>
                      {video.flaggedForBadPractice && (
                        <span className="badge badge-blocked" style={{ fontSize: '9px', padding: '1px 5px' }}>
                          FLAGGED
                        </span>
                      )}
                    </div>
                    <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>
                      By {video.mentorName} • {video.category}
                    </p>
                  </div>
                </div>

                <button
                  className="btn btn-outline btn-sm"
                  onClick={() => onInspectVideo(video)}
                >
                  <Play size={13} />
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
