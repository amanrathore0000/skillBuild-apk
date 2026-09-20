export type UserRole = 'LEARNER' | 'MENTOR' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'WARNED' | 'SUSPENDED' | 'BLOCKED';

export interface AdminUser {
  id: string;
  name: string;
  email: string;
  avatarUrl: string;
  role: UserRole;
  status: UserStatus;
  trustScore: number; // 0 - 100
  uploadsCount: number;
  swapCount: number;
  warningCount: number;
  phone: string;
  location: string;
  joinedDate: string;
  lastActive: string;
  reasonBlocked?: string;
  blockedAt?: string;
}

export type ContentStatus = 'APPROVED' | 'PENDING_REVIEW' | 'FLAGGED' | 'REMOVED';

export interface UploadedCourse {
  id: string;
  title: string;
  mentorId: string;
  mentorName: string;
  mentorAvatar: string;
  category: string;
  price: string;
  duration: string;
  lessonsCount: number;
  rating: number;
  status: ContentStatus;
  thumbnailUrl: string;
  flagReason?: string;
  createdAt: string;
}

export interface UploadedVideo {
  id: string;
  courseId: string;
  courseTitle: string;
  mentorName: string;
  mentorAvatar: string;
  title: string;
  videoUrl: string;
  thumbnailUrl: string;
  duration: string;
  views: number;
  likes: number;
  category: string;
  status: ContentStatus;
  flaggedForBadPractice: boolean;
  flagReason?: string;
  uploadDate: string;
}

export type ReportSeverity = 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
export type ReportType = 
  | 'INAPPROPRIATE_CONTENT' 
  | 'COPYRIGHT_INFRINGEMENT' 
  | 'HARASSMENT_OR_ABUSE' 
  | 'FRAUDULENT_SWAP' 
  | 'SPAM_OR_MALWARE';

export interface ModerationReport {
  id: string;
  reportedUserId: string;
  reportedUserName: string;
  reportedUserAvatar: string;
  reporterName: string;
  type: ReportType;
  severity: ReportSeverity;
  targetContent: string;
  targetType: 'VIDEO' | 'COURSE' | 'CHAT_MESSAGE';
  description: string;
  timestamp: string;
  status: 'PENDING' | 'RESOLVED' | 'DISMISSED';
}

export interface AuditLogEntry {
  id: string;
  adminName: string;
  action: 'USER_BLOCKED' | 'USER_UNBLOCKED' | 'USER_WARNED' | 'CONTENT_REMOVED' | 'REPORT_RESOLVED';
  target: string;
  details: string;
  timestamp: string;
  severity: 'INFO' | 'WARNING' | 'DANGER' | 'SUCCESS';
}

export interface PlatformStats {
  totalUsers: number;
  totalMentors: number;
  totalLearners: number;
  activeUsersToday: number;
  blockedUsersCount: number;
  warnedUsersCount: number;
  totalCoursesUploaded: number;
  totalVideosUploaded: number;
  totalHoursContent: number;
  pendingReportsCount: number;
}
