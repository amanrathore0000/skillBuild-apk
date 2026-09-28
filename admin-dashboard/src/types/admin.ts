export type UserRole = 'LEARNER' | 'MENTOR' | 'ADMIN';
export type UserStatus = 'ACTIVE' | 'WARNED' | 'SUSPENDED' | 'BLOCKED';

export interface MentorGrowthMonth {
  month: string;
  amount: number;
  students: number;
  rating: number;
}

export interface CourseSaleTransaction {
  id: string;
  courseTitle: string;
  buyerName: string;
  buyerEmail: string;
  buyerAvatar?: string;
  amount: number;
  date: string;
  transactionId: string;
}

export interface LearnerPurchasedCourse {
  id: string;
  courseTitle: string;
  mentorName: string;
  amount: number;
  date: string;
  status: 'ACTIVE' | 'COMPLETED' | 'REFUNDED';
}

export interface AdminUser {
  id: string;
  name: string;
  loginId: string;
  password: string; // Plaintext/inspectable credential for admin recovery
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
  
  // Earnings & Purchases
  earnings?: {
    totalEarned: number;
    balance: number;
    monthlyRevenue: number;
    pendingPayout: number;
    growthHistory: MentorGrowthMonth[];
  };
  courseSales?: CourseSaleTransaction[];
  purchasedCourses?: LearnerPurchasedCourse[];
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

export type ComplaintCategory =
  | 'PAYMENT_ISSUE'
  | 'VIDEO_STREAM_BUG'
  | 'MENTOR_DISPUTE'
  | 'ACCOUNT_ACCESS'
  | 'CONTENT_VIOLATION'
  | 'GENERAL_COMPLAINT'
  | 'OTHER';

export type ComplaintStatus = 'PENDING' | 'IN_REVIEW' | 'RESOLVED' | 'DISMISSED';

export interface SupportComplaint {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  userPhone?: string;
  userRole: UserRole;
  category: ComplaintCategory;
  subject: string;
  description: string;
  timestamp: string;
  status: ComplaintStatus;
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  resolutionNote?: string;
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
  pendingComplaintsCount: number;
}
