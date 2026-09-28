import {
  AdminUser,
  UploadedCourse,
  UploadedVideo,
  ModerationReport,
  AuditLogEntry,
  PlatformStats,
  UserStatus,
  SupportComplaint
} from '../types/admin';
import {
  INITIAL_USERS,
  INITIAL_COURSES,
  INITIAL_VIDEOS,
  INITIAL_REPORTS,
  INITIAL_AUDIT_LOGS,
  INITIAL_COMPLAINTS
} from './mockData';

const STORAGE_KEYS = {
  USERS: 'skillbuilder_admin_users_v1',
  COURSES: 'skillbuilder_admin_courses_v1',
  VIDEOS: 'skillbuilder_admin_videos_v1',
  REPORTS: 'skillbuilder_admin_reports_v1',
  LOGS: 'skillbuilder_admin_logs_v1',
  COMPLAINTS: 'skillbuilder_admin_complaints_v1',
  AUTH_SESSION: 'skillbuilder_admin_auth_v1'
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

  // ─── Help & Support Complaints ──────────────────────────────
  getComplaints(): SupportComplaint[] {
    return getStorage<SupportComplaint[]>(STORAGE_KEYS.COMPLAINTS, INITIAL_COMPLAINTS);
  },

  createComplaint(complaint: Omit<SupportComplaint, 'id' | 'timestamp' | 'status'>): SupportComplaint {
    const list = this.getComplaints();
    const newComplaint: SupportComplaint = {
      ...complaint,
      id: `cmp_${Date.now()}`,
      timestamp: 'Just now',
      status: 'PENDING'
    };
    setStorage(STORAGE_KEYS.COMPLAINTS, [newComplaint, ...list]);
    return newComplaint;
  },

  updateComplaintStatus(
    complaintId: string,
    status: SupportComplaint['status'],
    resolutionNote?: string
  ): { success: boolean; complaint?: SupportComplaint } {
    const complaints = this.getComplaints();
    const idx = complaints.findIndex(c => c.id === complaintId);
    if (idx === -1) return { success: false };

    complaints[idx] = {
      ...complaints[idx],
      status,
      resolutionNote: resolutionNote !== undefined ? resolutionNote : complaints[idx].resolutionNote
    };

    setStorage(STORAGE_KEYS.COMPLAINTS, complaints);

    this.addAuditLog({
      adminName: 'Chief Administrator',
      action: 'REPORT_RESOLVED',
      target: `Complaint #${complaintId} (${complaints[idx].category})`,
      details: `Complaint marked as ${status}. ${resolutionNote ? `Note: ${resolutionNote}` : ''}`,
      severity: status === 'RESOLVED' ? 'SUCCESS' : status === 'DISMISSED' ? 'WARNING' : 'INFO'
    });

    return { success: true, complaint: complaints[idx] };
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

    const complaints = this.getComplaints();

    const totalMentors = users.filter(u => u.role === 'MENTOR').length;
    const totalLearners = users.filter(u => u.role === 'LEARNER').length;
    const blockedCount = users.filter(u => u.status === 'BLOCKED' || u.status === 'SUSPENDED').length;
    const warnedCount = users.filter(u => u.status === 'WARNED').length;
    const pendingReports = reports.filter(r => r.status === 'PENDING').length;
    const pendingComplaints = complaints.filter(c => c.status === 'PENDING').length;

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
      pendingReportsCount: pendingReports,
      pendingComplaintsCount: pendingComplaints
    };
  },

  resetDemoData(): void {
    localStorage.removeItem(STORAGE_KEYS.USERS);
    localStorage.removeItem(STORAGE_KEYS.COURSES);
    localStorage.removeItem(STORAGE_KEYS.VIDEOS);
    localStorage.removeItem(STORAGE_KEYS.REPORTS);
    localStorage.removeItem(STORAGE_KEYS.LOGS);
    localStorage.removeItem(STORAGE_KEYS.COMPLAINTS);
  }
};

export interface AdminSession {
  token: string;
  adminId: string;
  name: string;
  email: string;
  role: string;
  loginTime: string;
  expiresAt: number;
  rememberMe: boolean;
}

export const AdminAuthService = {
  // Validate credentials through the backend and create persistent session
  async login(
    loginId: string,
    password: string,
    rememberMe: boolean = true
  ): Promise<{ success: boolean; session?: AdminSession; error?: string }> {
    const cleanId = loginId.trim();
    const cleanPass = password.trim();

    try {
      const response = await fetch('/api/v1/auth/admin/login', {
        method: 'POST',
        headers: {
          'Content-Type': 'application/json'
        },
        body: JSON.stringify({ loginId: cleanId, password: cleanPass })
      });

      const data = await response.json();

      if (!response.ok || !data.success) {
        return {
          success: false,
          error: data.message || 'Invalid Admin Login ID or Password. Please verify your credentials.'
        };
      }

      // Calculate session duration based on rememberMe: 7 days if rememberMe, 24 hours otherwise
      const durationMs = rememberMe ? 7 * 24 * 60 * 60 * 1000 : 24 * 60 * 60 * 1000;
      const expiresAt = Date.now() + durationMs;

      const session: AdminSession = {
        token: data.data.token,
        adminId: data.data.userId || 'adm_root_001',
        name: data.data.name || 'Super Administrator',
        email: data.data.email || 'admin@skillbuilder.io',
        role: data.data.role || 'SUPER_ADMIN',
        loginTime: new Date().toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' }),
        expiresAt,
        rememberMe
      };

      // Clear both storage locations first to avoid state leaks
      localStorage.removeItem(STORAGE_KEYS.AUTH_SESSION);
      sessionStorage.removeItem(STORAGE_KEYS.AUTH_SESSION);

      if (rememberMe) {
        localStorage.setItem(STORAGE_KEYS.AUTH_SESSION, JSON.stringify(session));
      } else {
        sessionStorage.setItem(STORAGE_KEYS.AUTH_SESSION, JSON.stringify(session));
      }

      AdminApiService.addAuditLog({
        adminName: session.name,
        action: 'REPORT_RESOLVED',
        target: 'Admin Console Access',
        details: `Administrator logged in successfully`,
        severity: 'SUCCESS'
      });

      return { success: true, session };
    } catch (e: any) {
      return {
        success: false,
        error: e.message || 'Unable to connect to backend authentication service.'
      };
    }
  },

  getSession(): AdminSession | null {
    try {
      const local = localStorage.getItem(STORAGE_KEYS.AUTH_SESSION);
      if (local) {
        return JSON.parse(local);
      }
      const session = sessionStorage.getItem(STORAGE_KEYS.AUTH_SESSION);
      if (session) {
        return JSON.parse(session);
      }
    } catch (e) {
      console.error(`Error reading ${STORAGE_KEYS.AUTH_SESSION} from storage`, e);
    }
    return null;
  },

  isAuthenticated(): boolean {
    const session = this.getSession();
    if (!session || !session.token) {
      return false;
    }

    // Enforce session expiration
    if (session.expiresAt && Date.now() > session.expiresAt) {
      this.logout();
      return false;
    }

    // Ensure session duration respects rememberMe: when false, must not be stored in localStorage
    if (!session.rememberMe && localStorage.getItem(STORAGE_KEYS.AUTH_SESSION)) {
      this.logout();
      return false;
    }

    // Verify token is a signed 3-part JWT
    const parts = session.token.split('.');
    if (parts.length !== 3) {
      this.logout();
      return false;
    }

    // Validate expiration from JWT payload if present
    try {
      const payload = JSON.parse(atob(parts[1]));
      if (payload.exp && Date.now() >= payload.exp * 1000) {
        this.logout();
        return false;
      }
    } catch {
      this.logout();
      return false;
    }

    // Validate token with backend if in browser environment
    try {
      if (typeof window !== 'undefined' && window.XMLHttpRequest) {
        const xhr = new XMLHttpRequest();
        xhr.open('GET', '/api/v1/auth/admin/verify', false);
        xhr.setRequestHeader('Authorization', `Bearer ${session.token}`);
        xhr.send();
        if (xhr.status === 401 || xhr.status === 403) {
          this.logout();
          return false;
        }
      }
    } catch {
      // Backend temporarily offline; rely on validated JWT expiration
    }

    return true;
  },

  logout(): void {
    const session = this.getSession();
    if (session) {
      AdminApiService.addAuditLog({
        adminName: session.name,
        action: 'REPORT_RESOLVED',
        target: 'Admin Console Access',
        details: `Administrator signed out of panel`,
        severity: 'INFO'
      });
    }
    localStorage.removeItem(STORAGE_KEYS.AUTH_SESSION);
    sessionStorage.removeItem(STORAGE_KEYS.AUTH_SESSION);
  }
};

