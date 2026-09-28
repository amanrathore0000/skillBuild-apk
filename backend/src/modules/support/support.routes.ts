import { Router, Request, Response } from 'express';

export const supportRouter = Router();

interface ComplaintItem {
  id: string;
  userId: string;
  userName: string;
  userEmail: string;
  userPhone?: string;
  userRole: 'LEARNER' | 'MENTOR';
  category: 'PAYMENT_ISSUE' | 'VIDEO_STREAM_BUG' | 'MENTOR_DISPUTE' | 'ACCOUNT_ACCESS' | 'CONTENT_VIOLATION' | 'GENERAL_COMPLAINT' | 'OTHER';
  subject: string;
  description: string;
  timestamp: string;
  status: 'PENDING' | 'IN_REVIEW' | 'RESOLVED' | 'DISMISSED';
  priority: 'LOW' | 'MEDIUM' | 'HIGH' | 'CRITICAL';
  resolutionNote?: string;
}

// In-memory / cache storage for complaints
const complaintsStore: ComplaintItem[] = [
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
  }
];

export const ALLOWED_STATUSES: readonly string[] = ['PENDING', 'IN_REVIEW', 'RESOLVED', 'DISMISSED'];
export const ALLOWED_CATEGORIES: readonly string[] = [
  'PAYMENT_ISSUE',
  'VIDEO_STREAM_BUG',
  'MENTOR_DISPUTE',
  'ACCOUNT_ACCESS',
  'CONTENT_VIOLATION',
  'GENERAL_COMPLAINT',
  'OTHER'
];
export const ALLOWED_PRIORITIES: readonly string[] = ['LOW', 'MEDIUM', 'HIGH', 'CRITICAL'];
export const ALLOWED_USER_ROLES: readonly string[] = ['LEARNER', 'MENTOR'];

// GET /api/v1/support/complaints
supportRouter.get('/complaints', (req: Request, res: Response) => {
  res.status(200).json({
    success: true,
    data: complaintsStore,
    total: complaintsStore.length
  });
});

// POST /api/v1/support/complaints
supportRouter.post('/complaints', (req: Request, res: Response) => {
  const {
    userId,
    userName,
    userEmail,
    userPhone,
    userRole,
    category,
    subject,
    description,
    priority
  } = req.body;

  if (!userName || !subject || !description) {
    res.status(400).json({
      success: false,
      message: 'userName, subject, and description are required fields.'
    });
    return;
  }

  if (userRole !== undefined && !ALLOWED_USER_ROLES.includes(userRole)) {
    res.status(400).json({
      success: false,
      message: `Invalid userRole '${userRole}'. Allowed values: ${ALLOWED_USER_ROLES.join(', ')}`
    });
    return;
  }

  if (category !== undefined && !ALLOWED_CATEGORIES.includes(category)) {
    res.status(400).json({
      success: false,
      message: `Invalid category '${category}'. Allowed values: ${ALLOWED_CATEGORIES.join(', ')}`
    });
    return;
  }

  if (priority !== undefined && !ALLOWED_PRIORITIES.includes(priority)) {
    res.status(400).json({
      success: false,
      message: `Invalid priority '${priority}'. Allowed values: ${ALLOWED_PRIORITIES.join(', ')}`
    });
    return;
  }

  const now = new Date();
  const timeFormatted = `${now.toLocaleDateString('en-US', { month: 'short', day: 'numeric' })}, ${now.toLocaleTimeString([], { hour: '2-digit', minute: '2-digit' })}`;

  const newComplaint: ComplaintItem = {
    id: `cmp_${Date.now()}`,
    userId: userId || `usr_${Date.now()}`,
    userName: userName || 'Anonymous App User',
    userEmail: userEmail || 'user@skillbuilder.io',
    userPhone: userPhone || '',
    userRole: userRole || 'LEARNER',
    category: category || 'GENERAL_COMPLAINT',
    subject,
    description,
    timestamp: timeFormatted,
    status: 'PENDING',
    priority: priority || 'MEDIUM'
  };

  complaintsStore.unshift(newComplaint);

  console.log(`[Support] New complaint received: ${newComplaint.id} - "${newComplaint.subject}" from ${newComplaint.userName}`);

  res.status(201).json({
    success: true,
    message: 'Complaint submitted successfully to administrative support.',
    data: newComplaint
  });
});

// PATCH /api/v1/support/complaints/:id
supportRouter.patch('/complaints/:id', (req: Request, res: Response) => {
  const { id } = req.params;
  const { status, resolutionNote } = req.body;

  const complaint = complaintsStore.find(c => c.id === id);
  if (!complaint) {
    res.status(404).json({
      success: false,
      message: 'Complaint not found.'
    });
    return;
  }

  if (status !== undefined) {
    if (!ALLOWED_STATUSES.includes(status)) {
      res.status(400).json({
        success: false,
        message: `Invalid status '${status}'. Allowed values: ${ALLOWED_STATUSES.join(', ')}`
      });
      return;
    }
    complaint.status = status as ComplaintItem['status'];
  }
  if (resolutionNote !== undefined) complaint.resolutionNote = resolutionNote;

  res.status(200).json({
    success: true,
    message: `Complaint #${id} updated successfully.`,
    data: complaint
  });
});
