import { AdminUser, UploadedCourse, UploadedVideo, ModerationReport, AuditLogEntry, SupportComplaint } from '../types/admin';

export const INITIAL_USERS: AdminUser[] = [
  // ─── MENTORS ────────────────────────────────────────────────
  {
    id: 'usr_1',
    name: 'Rohan Verma',
    loginId: 'rohan.design',
    password: 'RohanDesign@2026',
    email: 'rohan.design@skillbuilder.io',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300',
    role: 'MENTOR',
    status: 'ACTIVE',
    trustScore: 98,
    uploadsCount: 6,
    swapCount: 34,
    warningCount: 0,
    phone: '+91 98765 43210',
    location: 'Bengaluru, India',
    joinedDate: 'Jan 15, 2026',
    lastActive: '10 mins ago',
    earnings: {
      totalEarned: 5420.00,
      balance: 1240.50,
      monthlyRevenue: 1380.00,
      pendingPayout: 450.00,
      growthHistory: [
        { month: 'Jan', amount: 420, students: 8, rating: 4.8 },
        { month: 'Feb', amount: 680, students: 14, rating: 4.9 },
        { month: 'Mar', amount: 950, students: 22, rating: 4.9 },
        { month: 'Apr', amount: 1120, students: 28, rating: 4.95 },
        { month: 'May', amount: 1480, students: 36, rating: 4.95 },
        { month: 'Jun', amount: 1840, students: 48, rating: 4.98 },
      ]
    },
    courseSales: [
      {
        id: 'sale_101',
        courseTitle: 'Design Systems & Figma Mastery',
        buyerName: 'Ananya Deshmukh',
        buyerEmail: 'ananya.learner@gmail.com',
        buyerAvatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100',
        amount: 49.99,
        date: 'Sep 20, 2026',
        transactionId: 'TXN-FIG-8829'
      },
      {
        id: 'sale_102',
        courseTitle: 'Design Systems & Figma Mastery',
        buyerName: 'Rahul Mehra',
        buyerEmail: 'rahul.coder@yahoo.com',
        buyerAvatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100',
        amount: 49.99,
        date: 'Sep 18, 2026',
        transactionId: 'TXN-FIG-8812'
      },
      {
        id: 'sale_103',
        courseTitle: 'Advanced Micro-Interactions in Mobile UI',
        buyerName: 'Sneha Patel',
        buyerEmail: 'sneha.patel@outlook.com',
        buyerAvatar: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=100',
        amount: 39.00,
        date: 'Sep 15, 2026',
        transactionId: 'TXN-UI-7721'
      },
      {
        id: 'sale_104',
        courseTitle: 'Design Systems & Figma Mastery',
        buyerName: 'Vikram Rajput',
        buyerEmail: 'vikram.hacker@proton.me',
        buyerAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100',
        amount: 49.99,
        date: 'Sep 10, 2026',
        transactionId: 'TXN-FIG-7650'
      }
    ]
  },
  {
    id: 'usr_2',
    name: 'Aditi Sinha',
    loginId: 'aditi.pastry',
    password: 'AditiBakes#2026',
    email: 'aditi.pastry@gmail.com',
    avatarUrl: 'https://images.unsplash.com/photo-1494790108377-be9c29b29330?w=300',
    role: 'MENTOR',
    status: 'ACTIVE',
    trustScore: 96,
    uploadsCount: 8,
    swapCount: 42,
    warningCount: 0,
    phone: '+91 98231 11223',
    location: 'Mumbai, India',
    joinedDate: 'Feb 02, 2026',
    lastActive: '1 hour ago',
    earnings: {
      totalEarned: 3890.00,
      balance: 820.00,
      monthlyRevenue: 980.00,
      pendingPayout: 280.00,
      growthHistory: [
        { month: 'Feb', amount: 350, students: 9, rating: 4.8 },
        { month: 'Mar', amount: 560, students: 16, rating: 4.85 },
        { month: 'Apr', amount: 790, students: 22, rating: 4.9 },
        { month: 'May', amount: 1120, students: 30, rating: 4.92 },
        { month: 'Jun', amount: 1420, students: 38, rating: 4.96 },
      ]
    },
    courseSales: [
      {
        id: 'sale_201',
        courseTitle: 'Mastering Artisan French Baking',
        buyerName: 'Ananya Deshmukh',
        buyerEmail: 'ananya.learner@gmail.com',
        buyerAvatar: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=100',
        amount: 29.99,
        date: 'Sep 19, 2026',
        transactionId: 'TXN-BAKE-9112'
      },
      {
        id: 'sale_202',
        courseTitle: 'Mastering Artisan French Baking',
        buyerName: 'Kunal Kapoor',
        buyerEmail: 'kunal.k@gmail.com',
        buyerAvatar: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=100',
        amount: 29.99,
        date: 'Sep 12, 2026',
        transactionId: 'TXN-BAKE-8921'
      }
    ]
  },
  {
    id: 'usr_3',
    name: 'Priya Sharma',
    loginId: 'priya.android',
    password: 'PriyaAndroid$99',
    email: 'priya.dev@skillbuilder.io',
    avatarUrl: 'https://images.unsplash.com/photo-1517841905240-472988babdf9?w=300',
    role: 'MENTOR',
    status: 'ACTIVE',
    trustScore: 99,
    uploadsCount: 12,
    swapCount: 56,
    warningCount: 0,
    phone: '+91 91234 56789',
    location: 'Hyderabad, India',
    joinedDate: 'Jan 05, 2026',
    lastActive: '2 mins ago',
    earnings: {
      totalEarned: 8940.00,
      balance: 2450.00,
      monthlyRevenue: 2280.00,
      pendingPayout: 850.00,
      growthHistory: [
        { month: 'Jan', amount: 820, students: 18, rating: 4.9 },
        { month: 'Feb', amount: 1240, students: 28, rating: 4.92 },
        { month: 'Mar', amount: 1650, students: 40, rating: 4.95 },
        { month: 'Apr', amount: 2100, students: 54, rating: 4.97 },
        { month: 'May', amount: 2680, students: 68, rating: 4.99 },
        { month: 'Jun', amount: 3250, students: 82, rating: 4.99 },
      ]
    },
    courseSales: [
      {
        id: 'sale_301',
        courseTitle: 'Kotlin & Jetpack Compose 2026',
        buyerName: 'Raman Rathore',
        buyerEmail: 'raman.guitar@skillbuilder.io',
        buyerAvatar: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=100',
        amount: 59.99,
        date: 'Sep 21, 2026',
        transactionId: 'TXN-KOT-9988'
      },
      {
        id: 'sale_302',
        courseTitle: 'Kotlin & Jetpack Compose 2026',
        buyerName: 'Rahul Mehra',
        buyerEmail: 'rahul.coder@yahoo.com',
        buyerAvatar: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=100',
        amount: 59.99,
        date: 'Sep 17, 2026',
        transactionId: 'TXN-KOT-9844'
      }
    ]
  },
  {
    id: 'usr_4',
    name: 'Rohit Kumar (Bad Practice Demo)',
    loginId: 'rohit.scam99',
    password: 'HackerRohit#8899',
    email: 'rohit.scam99@tempmail.com',
    avatarUrl: 'https://images.unsplash.com/photo-1507003211169-0a1dd7228f2d?w=300',
    role: 'MENTOR',
    status: 'WARNED',
    trustScore: 42,
    uploadsCount: 2,
    swapCount: 1,
    warningCount: 2,
    phone: '+91 99887 76655',
    location: 'Delhi, India',
    joinedDate: 'Mar 10, 2026',
    lastActive: 'Yesterday',
    reasonBlocked: 'Flagged for uploading copyrighted video course from YouTube and misleading learners.',
    earnings: {
      totalEarned: 120.00,
      balance: 0.00,
      monthlyRevenue: 0.00,
      pendingPayout: 0.00,
      growthHistory: [
        { month: 'Mar', amount: 120, students: 2, rating: 2.1 },
        { month: 'Apr', amount: 0, students: 0, rating: 1.8 }
      ]
    },
    courseSales: [
      {
        id: 'sale_401',
        courseTitle: 'Full Stack MERN Clone',
        buyerName: 'Ananya Deshmukh',
        buyerEmail: 'ananya.learner@gmail.com',
        amount: 20.00,
        date: 'Mar 12, 2026',
        transactionId: 'TXN-BAD-1201'
      }
    ]
  },
  {
    id: 'usr_7',
    name: 'Raman Rathore',
    loginId: 'raman.guitar',
    password: 'AcousticRaman*77',
    email: 'raman.guitar@skillbuilder.io',
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300',
    role: 'MENTOR',
    status: 'ACTIVE',
    trustScore: 97,
    uploadsCount: 9,
    swapCount: 28,
    warningCount: 0,
    phone: '+91 98450 67890',
    location: 'Chandigarh, India',
    joinedDate: 'Jan 22, 2026',
    lastActive: '45 mins ago',
    earnings: {
      totalEarned: 4350.00,
      balance: 960.00,
      monthlyRevenue: 1100.00,
      pendingPayout: 320.00,
      growthHistory: [
        { month: 'Jan', amount: 320, students: 6, rating: 4.8 },
        { month: 'Feb', amount: 650, students: 15, rating: 4.85 },
        { month: 'Mar', amount: 980, students: 24, rating: 4.9 },
        { month: 'Apr', amount: 1320, students: 32, rating: 4.94 },
        { month: 'May', amount: 1780, students: 44, rating: 4.96 }
      ]
    },
    courseSales: [
      {
        id: 'sale_701',
        courseTitle: 'Acoustic Fingerstyle & Harmonic Masterclass',
        buyerName: 'Sneha Patel',
        buyerEmail: 'sneha.patel@outlook.com',
        amount: 39.99,
        date: 'Sep 19, 2026',
        transactionId: 'TXN-GTR-3321'
      }
    ]
  },
  {
    id: 'usr_8',
    name: 'Carlos Mendez',
    loginId: 'carlos.spanish',
    password: 'HolaAmigo#2026',
    email: 'carlos.spanish@skillbuilder.io',
    avatarUrl: 'https://images.unsplash.com/photo-1472099645785-5658abf4ff4e?w=300',
    role: 'MENTOR',
    status: 'ACTIVE',
    trustScore: 94,
    uploadsCount: 5,
    swapCount: 19,
    warningCount: 0,
    phone: '+34 612 345 678',
    location: 'Madrid, Spain',
    joinedDate: 'Feb 12, 2026',
    lastActive: '3 hours ago',
    earnings: {
      totalEarned: 2950.00,
      balance: 620.00,
      monthlyRevenue: 850.00,
      pendingPayout: 190.00,
      growthHistory: [
        { month: 'Feb', amount: 310, students: 8, rating: 4.7 },
        { month: 'Mar', amount: 590, students: 16, rating: 4.8 },
        { month: 'Apr', amount: 920, students: 25, rating: 4.9 }
      ]
    },
    courseSales: [
      {
        id: 'sale_801',
        courseTitle: 'Conversational Spanish for Professionals',
        buyerName: 'Rahul Mehra',
        buyerEmail: 'rahul.coder@yahoo.com',
        amount: 35.00,
        date: 'Sep 14, 2026',
        transactionId: 'TXN-SPA-4491'
      }
    ]
  },

  // ─── LEARNERS ───────────────────────────────────────────────
  {
    id: 'usr_5',
    name: 'Vikram Rajput',
    loginId: 'vikram.hacker',
    password: 'VikramSecret!89',
    email: 'vikram.hacker@proton.me',
    avatarUrl: 'https://images.unsplash.com/photo-1500648767791-00dcc994a43e?w=300',
    role: 'LEARNER',
    status: 'BLOCKED',
    trustScore: 12,
    uploadsCount: 0,
    swapCount: 0,
    warningCount: 3,
    phone: '+91 90000 12345',
    location: 'Pune, India',
    joinedDate: 'Mar 01, 2026',
    lastActive: '3 days ago',
    reasonBlocked: 'Attempted account spoofing, harassment in peer encrypted chat, and multiple community reports.',
    blockedAt: 'Mar 16, 2026',
    purchasedCourses: [
      {
        id: 'pur_501',
        courseTitle: 'Design Systems & Figma Mastery',
        mentorName: 'Rohan Verma',
        amount: 49.99,
        date: 'Sep 10, 2026',
        status: 'REFUNDED'
      }
    ]
  },
  {
    id: 'usr_6',
    name: 'Ananya Deshmukh',
    loginId: 'ananya.learns',
    password: 'AnanyaLearner@123',
    email: 'ananya.learner@gmail.com',
    avatarUrl: 'https://images.unsplash.com/photo-1544005313-94ddf0286df2?w=300',
    role: 'LEARNER',
    status: 'ACTIVE',
    trustScore: 92,
    uploadsCount: 0,
    swapCount: 15,
    warningCount: 0,
    phone: '+91 97654 32190',
    location: 'Jaipur, India',
    joinedDate: 'Feb 18, 2026',
    lastActive: 'Just now',
    purchasedCourses: [
      {
        id: 'pur_601',
        courseTitle: 'Design Systems & Figma Mastery',
        mentorName: 'Rohan Verma',
        amount: 49.99,
        date: 'Sep 20, 2026',
        status: 'ACTIVE'
      },
      {
        id: 'pur_602',
        courseTitle: 'Mastering Artisan French Baking',
        mentorName: 'Aditi Sinha',
        amount: 29.99,
        date: 'Sep 19, 2026',
        status: 'ACTIVE'
      }
    ]
  },
  {
    id: 'usr_9',
    name: 'Rahul Mehra',
    loginId: 'rahul.coder',
    password: 'RahulDev#Code99',
    email: 'rahul.coder@yahoo.com',
    avatarUrl: 'https://images.unsplash.com/photo-1506794778202-cad84cf45f1d?w=300',
    role: 'LEARNER',
    status: 'ACTIVE',
    trustScore: 95,
    uploadsCount: 0,
    swapCount: 22,
    warningCount: 0,
    phone: '+91 99112 33445',
    location: 'Noida, India',
    joinedDate: 'Jan 28, 2026',
    lastActive: '20 mins ago',
    purchasedCourses: [
      {
        id: 'pur_901',
        courseTitle: 'Kotlin & Jetpack Compose 2026',
        mentorName: 'Priya Sharma',
        amount: 59.99,
        date: 'Sep 17, 2026',
        status: 'ACTIVE'
      },
      {
        id: 'pur_902',
        courseTitle: 'Conversational Spanish for Professionals',
        mentorName: 'Carlos Mendez',
        amount: 35.00,
        date: 'Sep 14, 2026',
        status: 'ACTIVE'
      }
    ]
  },
  {
    id: 'usr_10',
    name: 'Sneha Patel',
    loginId: 'sneha.patel',
    password: 'SnehaStudy$2026',
    email: 'sneha.patel@outlook.com',
    avatarUrl: 'https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=300',
    role: 'LEARNER',
    status: 'ACTIVE',
    trustScore: 98,
    uploadsCount: 0,
    swapCount: 11,
    warningCount: 0,
    phone: '+91 98980 12345',
    location: 'Ahmedabad, India',
    joinedDate: 'Feb 10, 2026',
    lastActive: '4 hours ago',
    purchasedCourses: [
      {
        id: 'pur_1001',
        courseTitle: 'Acoustic Fingerstyle & Harmonic Masterclass',
        mentorName: 'Raman Rathore',
        amount: 39.99,
        date: 'Sep 19, 2026',
        status: 'ACTIVE'
      }
    ]
  }
];

export const INITIAL_COMPLAINTS: SupportComplaint[] = [
  {
    id: 'cmp_101',
    userId: 'usr_6',
    userName: 'Ananya Deshmukh',
    userEmail: 'ananya.learner@gmail.com',
    userPhone: '+91 97654 32190',
    userRole: 'LEARNER',
    category: 'PAYMENT_ISSUE',
    subject: 'Double charge occurred during Figma course unlock',
    description: 'I was charged twice on my card (TXN-FIG-8829 and TXN-FIG-8830) when unlocking the Design Systems course by mentor Rohan Verma. Kindly refund the duplicate payment.',
    timestamp: 'Today at 11:20 AM',
    status: 'PENDING',
    priority: 'HIGH'
  },
  {
    id: 'cmp_102',
    userId: 'usr_9',
    userName: 'Rahul Mehra',
    userEmail: 'rahul.coder@yahoo.com',
    userPhone: '+91 99112 33445',
    userRole: 'LEARNER',
    category: 'VIDEO_STREAM_BUG',
    subject: 'Lesson 3 in Kotlin Compose stops buffering after 5 minutes',
    description: 'During playback of Lesson 3 (StateFlow Architecture), the video stream pauses and shows an ExoPlayer network timeout error on my Android device.',
    timestamp: 'Today at 09:15 AM',
    status: 'PENDING',
    priority: 'MEDIUM'
  },
  {
    id: 'cmp_103',
    userId: 'usr_1',
    userName: 'Rohan Verma',
    userEmail: 'rohan.design@skillbuilder.io',
    userPhone: '+91 98765 43210',
    userRole: 'MENTOR',
    category: 'MENTOR_DISPUTE',
    subject: 'User Vikram Rajput sending offensive messages after rejected swap',
    description: 'Learner Vikram Rajput requested a reciprocal swap without having any verified teaching skills, and when I politely declined, he threatened to mass report my profile.',
    timestamp: 'Yesterday at 04:45 PM',
    status: 'IN_REVIEW',
    priority: 'CRITICAL',
    resolutionNote: 'Investigating user account. Action taken to suspend offending user.'
  },
  {
    id: 'cmp_104',
    userId: 'usr_10',
    userName: 'Sneha Patel',
    userEmail: 'sneha.patel@outlook.com',
    userPhone: '+91 98980 12345',
    userRole: 'LEARNER',
    category: 'ACCOUNT_ACCESS',
    subject: 'Unable to update my login phone number in settings',
    description: 'When I change my phone number in profile settings, the OTP prompt freezes. Please assist in updating to +91 98980 12345.',
    timestamp: 'Sep 19, 2026',
    status: 'RESOLVED',
    priority: 'LOW',
    resolutionNote: 'Updated phone number manually and verified SMS gateway.'
  }
];

export const INITIAL_COURSES: UploadedCourse[] = [];
export const INITIAL_VIDEOS: UploadedVideo[] = [];
export const INITIAL_REPORTS: ModerationReport[] = [];
export const INITIAL_AUDIT_LOGS: AuditLogEntry[] = [];
