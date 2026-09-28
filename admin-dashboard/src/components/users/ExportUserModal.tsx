import React, { useState, useMemo } from 'react';
import { AdminUser, UserRole } from '../../types/admin';
import {
  Download,
  X,
  Calendar,
  Filter,
  FileSpreadsheet,
  FileCode,
  CheckCircle2,
  Users,
  ShieldCheck,
  TrendingUp,
  Clock,
  ArrowRight
} from 'lucide-react';

export type TimePeriodFilter = 
  | 'ALL_TIME' 
  | 'TODAY' 
  | '7_DAYS' 
  | '30_DAYS' 
  | '90_DAYS' 
  | 'YTD' 
  | 'CUSTOM';

interface ExportUserModalProps {
  isOpen: boolean;
  onClose: () => void;
  users: AdminUser[];
}

export const ExportUserModal: React.FC<ExportUserModalProps> = ({
  isOpen,
  onClose,
  users
}) => {
  const [timePeriod, setTimePeriod] = useState<TimePeriodFilter>('ALL_TIME');
  const [dateFieldBasis, setDateFieldBasis] = useState<'JOINED' | 'LAST_ACTIVE'>('JOINED');
  const [roleFilter, setRoleFilter] = useState<'ALL' | UserRole>('ALL');
  const [statusFilter, setStatusFilter] = useState<'ALL' | 'ACTIVE' | 'WARNED' | 'BLOCKED'>('ALL');
  const [exportFormat, setExportFormat] = useState<'CSV' | 'JSON'>('CSV');

  // Custom date range state
  const [customStartDate, setCustomStartDate] = useState<string>('2026-01-01');
  const [customEndDate, setCustomEndDate] = useState<string>('2026-12-31');

  // Download state
  const [isExporting, setIsExporting] = useState<boolean>(false);
  const [exportSuccess, setExportSuccess] = useState<string | null>(null);

  // Helper to parse date
  const parseUserJoinedDate = (dateStr: string): Date | null => {
    try {
      const parsed = new Date(dateStr);
      if (!isNaN(parsed.getTime())) return parsed;
      return null;
    } catch {
      return null;
    }
  };

  // Helper for last active comparison
  const matchesLastActiveWindow = (lastActive: string, days: number): boolean => {
    const la = lastActive.toLowerCase();
    if (la.includes('just now') || la.includes('min') || la.includes('hour')) return true;
    if (days >= 1 && (la.includes('yesterday') || la.includes('1 day'))) return true;
    if (days >= 7 && (la.includes('days') || la.includes('3 days') || la.includes('5 days'))) return true;
    if (days >= 30) return true;
    return false;
  };

  // Filter users matching criteria
  const matchingUsers = useMemo(() => {
    const now = new Date();

    return users.filter(user => {
      // 1. Role filter
      if (roleFilter !== 'ALL' && user.role !== roleFilter) return false;

      // 2. Status filter
      if (statusFilter !== 'ALL') {
        if (statusFilter === 'BLOCKED' && !(user.status === 'BLOCKED' || user.status === 'SUSPENDED')) {
          return false;
        } else if (statusFilter !== 'BLOCKED' && user.status !== statusFilter) {
          return false;
        }
      }

      // 3. Time Period filter
      if (timePeriod === 'ALL_TIME') return true;

      if (dateFieldBasis === 'LAST_ACTIVE') {
        if (timePeriod === 'TODAY') return matchesLastActiveWindow(user.lastActive, 1);
        if (timePeriod === '7_DAYS') return matchesLastActiveWindow(user.lastActive, 7);
        if (timePeriod === '30_DAYS') return matchesLastActiveWindow(user.lastActive, 30);
        if (timePeriod === '90_DAYS' || timePeriod === 'YTD') return true;
        return true;
      }

      // Basis: Joined Date
      const userDate = parseUserJoinedDate(user.joinedDate);
      if (!userDate) return true; // fallback to include

      const diffMs = now.getTime() - userDate.getTime();
      const diffDays = diffMs / (1000 * 60 * 60 * 24);

      if (timePeriod === 'TODAY') {
        return diffDays <= 1;
      } else if (timePeriod === '7_DAYS') {
        return diffDays <= 7;
      } else if (timePeriod === '30_DAYS') {
        return diffDays <= 30;
      } else if (timePeriod === '90_DAYS') {
        return diffDays <= 90;
      } else if (timePeriod === 'YTD') {
        return userDate.getFullYear() >= 2026;
      } else if (timePeriod === 'CUSTOM') {
        const start = new Date(customStartDate);
        const end = new Date(customEndDate);
        end.setHours(23, 59, 59, 999);
        return userDate >= start && userDate <= end;
      }

      return true;
    });
  }, [users, timePeriod, dateFieldBasis, roleFilter, statusFilter, customStartDate, customEndDate]);

  // Mentors and Learners count in matching
  const matchingMentors = matchingUsers.filter(u => u.role === 'MENTOR').length;
  const matchingLearners = matchingUsers.filter(u => u.role === 'LEARNER').length;

  // Escape helper for CSV
  const escapeCSV = (val: any): string => {
    if (val === null || val === undefined) return '""';
    let str = String(val);
    if (/^[=+\-@\t\r]/.test(str)) {
      str = `'${str}`;
    }
    const escaped = str.replace(/"/g, '""');
    return `"${escaped}"`;
  };

  // Generate CSV string
  const generateCSV = (data: AdminUser[]): string => {
    const headers = [
      'User ID',
      'Full Name',
      'Login ID',
      'Email Address',
      'Phone Number',
      'Location / City',
      'Role',
      'Account Status',
      'Trust Score (%)',
      'Warning Count',
      'Registration Date',
      'Last Active',
      'Total Uploads',
      'Total Swaps',
      'Total Earned ($)',
      'Available Balance ($)',
      'Monthly Revenue ($)',
      'Pending Payout ($)',
      'Courses Purchased Count',
      'Total Spent on Courses ($)',
      'Moderation Reason',
      'Blocked / Suspended Date'
    ];

    const rows = data.map(u => {
      const totalEarned = u.earnings?.totalEarned ? u.earnings.totalEarned.toFixed(2) : '0.00';
      const balance = u.earnings?.balance ? u.earnings.balance.toFixed(2) : '0.00';
      const monthlyRev = u.earnings?.monthlyRevenue ? u.earnings.monthlyRevenue.toFixed(2) : '0.00';
      const pendingPayout = u.earnings?.pendingPayout ? u.earnings.pendingPayout.toFixed(2) : '0.00';
      
      const purchasedCount = u.purchasedCourses ? u.purchasedCourses.length : 0;
      const totalSpent = u.purchasedCourses 
        ? u.purchasedCourses.reduce((sum, item) => sum + (item.amount || 0), 0).toFixed(2)
        : '0.00';

      return [
        escapeCSV(u.id),
        escapeCSV(u.name),
        escapeCSV(u.loginId || 'N/A'),
        escapeCSV(u.email),
        escapeCSV(u.phone || 'N/A'),
        escapeCSV(u.location || 'N/A'),
        escapeCSV(u.role),
        escapeCSV(u.status),
        escapeCSV(u.trustScore),
        escapeCSV(u.warningCount),
        escapeCSV(u.joinedDate),
        escapeCSV(u.lastActive),
        escapeCSV(u.uploadsCount),
        escapeCSV(u.swapCount),
        escapeCSV(totalEarned),
        escapeCSV(balance),
        escapeCSV(monthlyRev),
        escapeCSV(pendingPayout),
        escapeCSV(purchasedCount),
        escapeCSV(totalSpent),
        escapeCSV(u.reasonBlocked || 'N/A'),
        escapeCSV(u.blockedAt || 'N/A')
      ].join(',');
    });

    return [headers.join(','), ...rows].join('\r\n');
  };

  // Generate JSON string
  const generateJSON = (data: AdminUser[]): string => {
    const sanitized = data.map(u => ({
      id: u.id,
      name: u.name,
      loginId: u.loginId,
      email: u.email,
      phone: u.phone,
      location: u.location,
      role: u.role,
      status: u.status,
      trustScore: u.trustScore,
      warningCount: u.warningCount,
      joinedDate: u.joinedDate,
      lastActive: u.lastActive,
      uploadsCount: u.uploadsCount,
      swapCount: u.swapCount,
      earnings: u.earnings || null,
      courseSales: u.courseSales || [],
      purchasedCourses: u.purchasedCourses || [],
      reasonBlocked: u.reasonBlocked || null,
      blockedAt: u.blockedAt || null,
      exportedAt: new Date().toISOString()
    }));
    return JSON.stringify(sanitized, null, 2);
  };

  // Trigger browser download
  const handleDownload = () => {
    setIsExporting(true);
    setExportSuccess(null);

    setTimeout(() => {
      try {
        const dateTag = new Date().toISOString().slice(0, 10);
        const periodTag = timePeriod.toLowerCase().replace('_', '-');
        const roleTag = roleFilter.toLowerCase();
        const baseFilename = `skillbuilder-users-${roleTag}-${periodTag}-${dateTag}`;

        let blob: Blob;
        let filename: string;

        if (exportFormat === 'CSV') {
          const csvContent = generateCSV(matchingUsers);
          blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
          filename = `${baseFilename}.csv`;
        } else {
          const jsonContent = generateJSON(matchingUsers);
          blob = new Blob([jsonContent], { type: 'application/json;charset=utf-8;' });
          filename = `${baseFilename}.json`;
        }

        const url = URL.createObjectURL(blob);
        const link = document.createElement('a');
        link.href = url;
        link.setAttribute('download', filename);
        document.body.appendChild(link);
        link.click();
        document.body.removeChild(link);
        URL.revokeObjectURL(url);

        setIsExporting(false);
        setExportSuccess(`Successfully downloaded ${matchingUsers.length} user records (${filename})`);
      } catch (err) {
        console.error('Download failed:', err);
        setIsExporting(false);
      }
    }, 400);
  };

  if (!isOpen) return null;

  return (
    <div className="modal-overlay" onClick={onClose}>
      <div 
        className="modal-container" 
        onClick={(e) => e.stopPropagation()}
        style={{ maxWidth: '640px', width: '92%' }}
      >
        {/* Header */}
        <div className="modal-header">
          <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
            <div
              style={{
                width: '42px',
                height: '42px',
                borderRadius: '10px',
                backgroundColor: '#09090b',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                color: '#ffffff'
              }}
            >
              <Download size={22} />
            </div>
            <div>
              <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#09090b' }}>
                Download User Data
              </h3>
              <p style={{ fontSize: '12px', color: '#71717a', fontWeight: 500 }}>
                Export complete mentor & learner records filtered by time period
              </p>
            </div>
          </div>
          <button
            onClick={onClose}
            style={{
              background: '#f4f4f5',
              border: 'none',
              borderRadius: '50%',
              width: '32px',
              height: '32px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              cursor: 'pointer',
              color: '#71717a'
            }}
          >
            <X size={16} />
          </button>
        </div>

        {/* Modal Body */}
        <div className="modal-body" style={{ display: 'flex', flexDirection: 'column', gap: '20px' }}>
          
          {/* 1. Time Period Selector */}
          <div>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '8px' }}>
              <label style={{ fontSize: '13px', fontWeight: 800, color: '#09090b', display: 'flex', alignItems: 'center', gap: '6px' }}>
                <Calendar size={15} color="#10b981" />
                Select Time Period
              </label>
              <div style={{ display: 'flex', gap: '4px', background: '#f4f4f5', padding: '2px', borderRadius: '6px' }}>
                <button
                  type="button"
                  onClick={() => setDateFieldBasis('JOINED')}
                  style={{
                    border: 'none',
                    padding: '3px 8px',
                    fontSize: '11px',
                    fontWeight: 700,
                    borderRadius: '4px',
                    cursor: 'pointer',
                    background: dateFieldBasis === 'JOINED' ? '#09090b' : 'transparent',
                    color: dateFieldBasis === 'JOINED' ? '#ffffff' : '#71717a'
                  }}
                >
                  Joined Date
                </button>
                <button
                  type="button"
                  onClick={() => setDateFieldBasis('LAST_ACTIVE')}
                  style={{
                    border: 'none',
                    padding: '3px 8px',
                    fontSize: '11px',
                    fontWeight: 700,
                    borderRadius: '4px',
                    cursor: 'pointer',
                    background: dateFieldBasis === 'LAST_ACTIVE' ? '#09090b' : 'transparent',
                    color: dateFieldBasis === 'LAST_ACTIVE' ? '#ffffff' : '#71717a'
                  }}
                >
                  Last Active
                </button>
              </div>
            </div>

            {/* Time Period Presets Grid */}
            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(3, 1fr)', gap: '8px' }}>
              {[
                { id: 'ALL_TIME', label: 'All Time', sub: 'Entire Database' },
                { id: 'TODAY', label: 'Today', sub: 'Past 24 Hours' },
                { id: '7_DAYS', label: 'Last 7 Days', sub: 'Past Week' },
                { id: '30_DAYS', label: 'Last 30 Days', sub: 'Past Month' },
                { id: '90_DAYS', label: 'Last 90 Days', sub: 'Past Quarter' },
                { id: 'CUSTOM', label: 'Custom Range', sub: 'Pick Dates' }
              ].map((p) => (
                <button
                  key={p.id}
                  type="button"
                  onClick={() => setTimePeriod(p.id as TimePeriodFilter)}
                  style={{
                    padding: '10px 12px',
                    borderRadius: '8px',
                    border: timePeriod === p.id ? '2px solid #09090b' : '1px solid #e4e4e7',
                    background: timePeriod === p.id ? '#09090b' : '#ffffff',
                    color: timePeriod === p.id ? '#ffffff' : '#09090b',
                    textAlign: 'left',
                    cursor: 'pointer',
                    transition: 'all 0.15s ease',
                    display: 'flex',
                    flexDirection: 'column',
                    gap: '2px'
                  }}
                >
                  <span style={{ fontSize: '13px', fontWeight: 800 }}>{p.label}</span>
                  <span style={{ fontSize: '11px', color: timePeriod === p.id ? '#a1a1aa' : '#71717a', fontWeight: 500 }}>
                    {p.sub}
                  </span>
                </button>
              ))}
            </div>

            {/* Custom Date Pickers (Shown if CUSTOM selected) */}
            {timePeriod === 'CUSTOM' && (
              <div 
                style={{ 
                  marginTop: '12px', 
                  padding: '12px', 
                  background: '#f9fafb', 
                  borderRadius: '8px',
                  border: '1px solid #e4e4e7',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '12px'
                }}
              >
                <div style={{ flex: 1 }}>
                  <label style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', display: 'block', marginBottom: '4px' }}>
                    FROM DATE
                  </label>
                  <input
                    type="date"
                    className="select-input"
                    value={customStartDate}
                    onChange={(e) => setCustomStartDate(e.target.value)}
                    style={{ width: '100%', fontSize: '13px', fontWeight: 600 }}
                  />
                </div>
                <ArrowRight size={16} color="#71717a" style={{ marginTop: '16px' }} />
                <div style={{ flex: 1 }}>
                  <label style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', display: 'block', marginBottom: '4px' }}>
                    TO DATE
                  </label>
                  <input
                    type="date"
                    className="select-input"
                    value={customEndDate}
                    onChange={(e) => setCustomEndDate(e.target.value)}
                    style={{ width: '100%', fontSize: '13px', fontWeight: 600 }}
                  />
                </div>
              </div>
            )}
          </div>

          {/* 2. Target Filters (Role & Status) */}
          <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '12px' }}>
            <div>
              <label style={{ fontSize: '12px', fontWeight: 800, color: '#09090b', display: 'block', marginBottom: '6px' }}>
                Account Role
              </label>
              <select
                className="select-input"
                style={{ width: '100%', fontWeight: 700 }}
                value={roleFilter}
                onChange={(e) => setRoleFilter(e.target.value as any)}
              >
                <option value="ALL">All Roles (Mentors & Learners)</option>
                <option value="MENTOR">Mentors Only</option>
                <option value="LEARNER">Learners Only</option>
              </select>
            </div>

            <div>
              <label style={{ fontSize: '12px', fontWeight: 800, color: '#09090b', display: 'block', marginBottom: '6px' }}>
                Account Status
              </label>
              <select
                className="select-input"
                style={{ width: '100%', fontWeight: 700 }}
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as any)}
              >
                <option value="ALL">All Statuses</option>
                <option value="ACTIVE">Active Only</option>
                <option value="WARNED">Warned Only</option>
                <option value="BLOCKED">Blocked / Suspended</option>
              </select>
            </div>
          </div>

          {/* 3. Export Format Selector */}
          <div>
            <label style={{ fontSize: '12px', fontWeight: 800, color: '#09090b', display: 'block', marginBottom: '6px' }}>
              Export File Format
            </label>
            <div style={{ display: 'grid', gridTemplateColumns: '1fr 1fr', gap: '10px' }}>
              <button
                type="button"
                onClick={() => setExportFormat('CSV')}
                style={{
                  padding: '10px 14px',
                  borderRadius: '8px',
                  border: exportFormat === 'CSV' ? '2px solid #10b981' : '1px solid #e4e4e7',
                  background: exportFormat === 'CSV' ? '#ecfdf5' : '#ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  cursor: 'pointer',
                  textAlign: 'left'
                }}
              >
                <FileSpreadsheet size={20} color={exportFormat === 'CSV' ? '#10b981' : '#71717a'} />
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 800, color: exportFormat === 'CSV' ? '#065f46' : '#09090b' }}>
                    CSV Spreadsheet (.csv)
                  </div>
                  <div style={{ fontSize: '11px', color: '#71717a' }}>
                    For Excel, Google Sheets, Numbers
                  </div>
                </div>
              </button>

              <button
                type="button"
                onClick={() => setExportFormat('JSON')}
                style={{
                  padding: '10px 14px',
                  borderRadius: '8px',
                  border: exportFormat === 'JSON' ? '2px solid #09090b' : '1px solid #e4e4e7',
                  background: exportFormat === 'JSON' ? '#f4f4f5' : '#ffffff',
                  display: 'flex',
                  alignItems: 'center',
                  gap: '10px',
                  cursor: 'pointer',
                  textAlign: 'left'
                }}
              >
                <FileCode size={20} color={exportFormat === 'JSON' ? '#09090b' : '#71717a'} />
                <div>
                  <div style={{ fontSize: '13px', fontWeight: 800, color: '#09090b' }}>
                    Structured JSON (.json)
                  </div>
                  <div style={{ fontSize: '11px', color: '#71717a' }}>
                    Complete raw object hierarchy
                  </div>
                </div>
              </button>
            </div>
          </div>

          {/* 4. Live Record Count Summary Card */}
          <div
            style={{
              padding: '14px 18px',
              borderRadius: '10px',
              backgroundColor: '#f8fafc',
              border: '1px solid #e2e8f0',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'space-between'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
              <Users size={20} color="#09090b" />
              <div>
                <span style={{ fontSize: '14px', fontWeight: 800, color: '#09090b' }}>
                  {matchingUsers.length} User Records Matching
                </span>
                <p style={{ fontSize: '12px', color: '#64748b', margin: 0 }}>
                  Includes {matchingMentors} Mentors and {matchingLearners} Learners
                </p>
              </div>
            </div>
            <span
              style={{
                fontSize: '12px',
                fontWeight: 800,
                color: '#10b981',
                backgroundColor: '#ecfdf5',
                padding: '4px 10px',
                borderRadius: '20px',
                border: '1px solid #a7f3d0'
              }}
            >
              Ready to Export
            </span>
          </div>

          {/* Success Toast */}
          {exportSuccess && (
            <div
              style={{
                padding: '10px 14px',
                borderRadius: '8px',
                backgroundColor: '#ecfdf5',
                border: '1px solid #a7f3d0',
                color: '#065f46',
                fontSize: '13px',
                fontWeight: 700,
                display: 'flex',
                alignItems: 'center',
                gap: '8px'
              }}
            >
              <CheckCircle2 size={16} />
              {exportSuccess}
            </div>
          )}

        </div>

        {/* Modal Footer */}
        <div 
          className="modal-footer" 
          style={{ 
            display: 'flex', 
            justifyContent: 'space-between', 
            alignItems: 'center',
            borderTop: '1px solid #e4e4e7',
            paddingTop: '16px',
            marginTop: '8px'
          }}
        >
          <button
            type="button"
            onClick={onClose}
            className="btn btn-outline"
            style={{ fontWeight: 700 }}
          >
            Cancel
          </button>

          <button
            type="button"
            onClick={handleDownload}
            disabled={isExporting || matchingUsers.length === 0}
            className="btn btn-primary"
            style={{
              display: 'flex',
              alignItems: 'center',
              gap: '8px',
              backgroundColor: matchingUsers.length === 0 ? '#a1a1aa' : '#09090b',
              color: '#ffffff',
              fontWeight: 800,
              padding: '10px 20px',
              borderRadius: '8px',
              cursor: matchingUsers.length === 0 ? 'not-allowed' : 'pointer'
            }}
          >
            <Download size={16} />
            {isExporting 
              ? 'Generating File...' 
              : `Download ${matchingUsers.length} Records (${exportFormat})`}
          </button>
        </div>

      </div>
    </div>
  );
};
