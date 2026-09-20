import {
  AdminUser,
  UploadedCourse,
  UploadedVideo,
  ModerationReport,
  AuditLogEntry,
  PlatformStats,
  UserStatus
} from '../types/admin';
import {
  INITIAL_USERS,
  INITIAL_COURSES,
  INITIAL_VIDEOS,
  INITIAL_REPORTS,
  INITIAL_AUDIT_LOGS
} from './mockData';

const STORAGE_KEYS = {
  USERS: 'skillbuilder_admin_users_v1',
  COURSES: 'skillbuilder_admin_courses_v1',
  VIDEOS: 'skillbuilder_admin_videos_v1',
  REPORTS: 'skillbuilder_admin_reports_v1',
  LOGS: 'skillbuilder_admin_logs_v1',
};

function getStorage<T>(key: string, fallback: T): T {
  try {
    const item = localStorage.getItem(key);
    if (item) {
      return JSON.parse(item);
    }
  } catch (e) {
    console.error(`Error reading ${key} from storage`, e);
  }
  return fallback;
}

function setStorage<T>(key: string, value: T): void {
  try {
    localStorage.setItem(key, JSON.stringify(value));
  } catch (e) {
    console.error(`Error writing ${key} to storage`, e);
  }
}

export const AdminApiService = {
  // ─── Users ──────────────────────────────────────────────────
  getUsers(): AdminUser[] {
    return getStorage<AdminUser[]>(STORAGE_KEYS.USERS, INITIAL_USERS);
  },

  updateUserStatus(
    userId: string,
    newStatus: UserStatus,
    reason?: string
  ): { success: boolean; user?: AdminUser; error?: string } {
    const users = this.getUsers();
    const index = users.findIndex(u => u.id === userId);
    if (index === -1) {
      return { success: false, error: 'User not found' };
    }

    const updatedUser = {
      ...users[index],
      status: newStatus,
      reasonBlocked: newStatus === 'BLOCKED' || newStatus === 'SUSPENDED' ? reason : undefined,
      blockedAt: newStatus === 'BLOCKED' || newStatus === 'SUSPENDED' ? new Date().toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' }) : undefined,
      warningCount: newStatus === 'WARNED' ? users[index].warningCount + 1 : users[index].warningCount,
      trustScore: newStatus === 'BLOCKED' ? 10 : newStatus === 'WARNED' ? Math.max(20, users[index].trustScore - 25) : users[index].trustScore
    };

    users[index] = updatedUser;
    setStorage(STORAGE_KEYS.USERS, users);

    // Append to audit log
    const actionName = 
      newStatus === 'BLOCKED' ? 'USER_BLOCKED' :
      newStatus === 'ACTIVE' ? 'USER_UNBLOCKED' : 'USER_WARNED';

    this.addAuditLog({
      adminName: 'Chief Administrator',
      action: actionName,
      target: `${updatedUser.name} (${updatedUser.id})`,
      details: reason ? `Status changed to ${newStatus}. Reason: ${reason}` : `Status changed to ${newStatus}`,
      severity: newStatus === 'BLOCKED' ? 'DANGER' : newStatus === 'WARNED' ? 'WARNING' : 'SUCCESS'
    });

    return { success: true, user: updatedUser };
  },

  // ─── Content Uploads (Courses & Videos) ──────────────────────
  getCourses(): UploadedCourse[] {
    return getStorage<UploadedCourse[]>(STORAGE_KEYS.COURSES, INITIAL_COURSES);
  },

  getVideos(): UploadedVideo[] {
    return getStorage<UploadedVideo[]>(STORAGE_KEYS.VIDEOS, INITIAL_VIDEOS);
  },

  updateVideoStatus(
    videoId: string,
    status: 'APPROVED' | 'FLAGGED' | 'REMOVED',
    reason?: string
  ): { success: boolean; video?: UploadedVideo } {
    const videos = this.getVideos();
    const idx = videos.findIndex(v => v.id === videoId);
    if (idx === -1) return { success: false };

    videos[idx] = {
      ...videos[idx],
      status,
      flaggedForBadPractice: status === 'FLAGGED' || status === 'REMOVED',
      flagReason: reason || videos[idx].flagReason
    };

    setStorage(STORAGE_KEYS.VIDEOS, videos);

    this.addAuditLog({
      adminName: 'Chief Administrator',
      action: 'CONTENT_REMOVED',
      target: `Video: ${videos[idx].title}`,
      details: `Status set to ${status}. ${reason ? `Reason: ${reason}` : ''}`,
      severity: status === 'REMOVED' ? 'DANGER' : status === 'FLAGGED' ? 'WARNING' : 'SUCCESS'
    });

    return { success: true, video: videos[idx] };
  },

  updateCourseStatus(
    courseId: string,
    status: 'APPROVED' | 'FLAGGED' | 'REMOVED',
    reason?: string
  ): { success: boolean; course?: UploadedCourse } {
    const courses = this.getCourses();
    const idx = courses.findIndex(c => c.id === courseId);
    if (idx === -1) return { success: false };

    courses[idx] = {
      ...courses[idx],
      status,
      flagReason: reason || courses[idx].flagReason
    };

    setStorage(STORAGE_KEYS.COURSES, courses);

    this.addAuditLog({
      adminName: 'Chief Administrator',
      action: 'CONTENT_REMOVED',
      target: `Course: ${courses[idx].title}`,
      details: `Course marked as ${status}. ${reason ? `Reason: ${reason}` : ''}`,
      severity: status === 'REMOVED' ? 'DANGER' : status === 'FLAGGED' ? 'WARNING' : 'SUCCESS'
    });

    return { success: true, course: courses[idx] };
  },

  // ─── Moderation Reports ─────────────────────────────────────
  getReports(): ModerationReport[] {
    return getStorage<ModerationReport[]>(STORAGE_KEYS.REPORTS, INITIAL_REPORTS);
  },

  resolveReport(reportId: string, actionNote: string): void {
    const reports = this.getReports();
    const idx = reports.findIndex(r => r.id === reportId);
    if (idx !== -1) {
      reports[idx].status = 'RESOLVED';
      setStorage(STORAGE_KEYS.REPORTS, reports);

      this.addAuditLog({
        adminName: 'Chief Administrator',
        action: 'REPORT_RESOLVED',
        target: `Report #${reportId}`,
        details: `Resolved: ${actionNote}`,
        severity: 'SUCCESS'
      });
    }
  },

  dismissReport(reportId: string): void {
    const reports = this.getReports();
    const idx = reports.findIndex(r => r.id === reportId);
    if (idx !== -1) {
      reports[idx].status = 'DISMISSED';
      setStorage(STORAGE_KEYS.REPORTS, reports);
    }
  },

  // ─── Audit Logs ─────────────────────────────────────────────
  getAuditLogs(): AuditLogEntry[] {
    return getStorage<AuditLogEntry[]>(STORAGE_KEYS.LOGS, INITIAL_AUDIT_LOGS);
  },

  addAuditLog(entry: Omit<AuditLogEntry, 'id' | 'timestamp'>): void {
    const logs = this.getAuditLogs();
    const now = new Date();
    const formatted = `${now.toLocaleDateString('en-US', { month: 'short', day: 'numeric', year: 'numeric' })}, ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;
    
    const newLog: AuditLogEntry = {
      id: `log_${Date.now()}`,
      timestamp: formatted,
      ...entry
    };

    setStorage(STORAGE_KEYS.LOGS, [newLog, ...logs]);
  },

  // ─── Platform Aggregate Stats ───────────────────────────────
  getStats(): PlatformStats {
    const users = this.getUsers();
    const courses = this.getCourses();
    const videos = this.getVideos();
    const reports = this.getReports();

    const totalMentors = users.filter(u => u.role === 'MENTOR').length;
    const totalLearners = users.filter(u => u.role === 'LEARNER').length;
    const blockedCount = users.filter(u => u.status === 'BLOCKED' || u.status === 'SUSPENDED').length;
    const warnedCount = users.filter(u => u.status === 'WARNED').length;
    const pendingReports = reports.filter(r => r.status === 'PENDING').length;

    return {
      totalUsers: users.length,
      totalMentors,
      totalLearners,
      activeUsersToday: Math.round(users.length * 0.75),
      blockedUsersCount: blockedCount,
      warnedUsersCount: warnedCount,
      totalCoursesUploaded: courses.length,
      totalVideosUploaded: videos.length,
      totalHoursContent: 18.5,
      pendingReportsCount: pendingReports
    };
  },

  resetDemoData(): void {
    localStorage.removeItem(STORAGE_KEYS.USERS);
    localStorage.removeItem(STORAGE_KEYS.COURSES);
    localStorage.removeItem(STORAGE_KEYS.VIDEOS);
    localStorage.removeItem(STORAGE_KEYS.REPORTS);
    localStorage.removeItem(STORAGE_KEYS.LOGS);
  }
};
