import React, { useState } from 'react';
import { AdminUser } from '../../types/admin';
import {
  X,
  TrendingUp,
  DollarSign,
  Users,
  Star,
  BookOpen,
  ArrowUpRight,
  Award,
  Wallet,
  Calendar,
  Maximize2,
  Minimize2,
  ShieldCheck,
  CheckCircle2,
  Download,
  Clock,
  MapPin,
  Mail,
  Phone
} from 'lucide-react';

interface MentorAnalyticsDrawerProps {
  mentor: AdminUser | null;
  isOpen: boolean;
  onClose: () => void;
}

export const MentorAnalyticsDrawer: React.FC<MentorAnalyticsDrawerProps> = ({
  mentor,
  isOpen,
  onClose
}) => {
  // Default to Full Screen as requested
  const [isFullScreen, setIsFullScreen] = useState<boolean>(true);
  const [activeChartMetric, setActiveChartMetric] = useState<'REVENUE' | 'STUDENTS'>('REVENUE');

  if (!isOpen || !mentor) return null;

  const history = mentor.earnings?.growthHistory || [
    { month: 'Jan', amount: 450, students: 10, rating: 4.8 },
    { month: 'Feb', amount: 820, students: 20, rating: 4.85 },
    { month: 'Mar', amount: 1350, students: 34, rating: 4.9 },
    { month: 'Apr', amount: 1890, students: 48, rating: 4.92 },
    { month: 'May', amount: 2450, students: 62, rating: 4.96 },
    { month: 'Jun', amount: 3100, students: 78, rating: 4.98 }
  ];

  const maxAmount = Math.max(...history.map(h => h.amount), 100);
  const maxStudents = Math.max(...history.map(h => h.students), 10);
  const totalStudents = history.reduce((acc, curr) => acc + curr.students, 0);
  const avgRating = (
    history.reduce((acc, curr) => acc + curr.rating, 0) / (history.length || 1)
  ).toFixed(2);

  // SVG Chart Dimensions (Panoramic ViewBox)
  const chartWidth = 780;
  const chartHeight = 220;
  const paddingX = 55;
  const paddingY = 30;
  const usableWidth = chartWidth - paddingX * 2;
  const usableHeight = chartHeight - paddingY * 2;

  // Calculate coordinates for Revenue curve
  const points = history.map((item, index) => {
    const x = paddingX + (index / (history.length - 1 || 1)) * usableWidth;
    const y = chartHeight - paddingY - (item.amount / maxAmount) * usableHeight;
    return { x, y, item };
  });

  const pathD = points.reduce((acc, pt, idx) => {
    return idx === 0 ? `M ${pt.x} ${pt.y}` : `${acc} L ${pt.x} ${pt.y}`;
  }, '');

  const areaD = points.length > 0
    ? `${pathD} L ${points[points.length - 1].x} ${chartHeight - paddingY} L ${points[0].x} ${chartHeight - paddingY} Z`
    : '';

  // Download export summary handler
  const handleExportSummary = () => {
    const csvContent = [
      'Month,Revenue ($),Students Enrolled,Rating',
      ...history.map(h => `"${h.month}",${h.amount},${h.students},${h.rating}`)
    ].join('\r\n');

    const blob = new Blob(['\uFEFF' + csvContent], { type: 'text/csv;charset=utf-8;' });
    const url = URL.createObjectURL(blob);
    const link = document.createElement('a');
    link.href = url;
    link.setAttribute('download', `${mentor.loginId || mentor.id}-growth-analytics.csv`);
    document.body.appendChild(link);
    link.click();
    document.body.removeChild(link);
    URL.revokeObjectURL(url);
  };

  return (
    <div
      style={{
        position: 'fixed',
        inset: 0,
        backgroundColor: isFullScreen ? '#ffffff' : 'rgba(9, 9, 11, 0.45)',
        backdropFilter: isFullScreen ? 'none' : 'blur(4px)',
        zIndex: 1200,
        display: 'flex',
        justifyContent: isFullScreen ? 'center' : 'flex-end',
        alignItems: isFullScreen ? 'center' : 'stretch',
        animation: 'fadeIn 0.2s ease-out'
      }}
      onClick={isFullScreen ? undefined : onClose}
    >
      <div
        style={{
          width: isFullScreen ? '100vw' : '560px',
          maxWidth: '100%',
          height: isFullScreen ? '100vh' : '100%',
          backgroundColor: '#ffffff',
          boxShadow: isFullScreen ? 'none' : '-8px 0 32px rgba(0, 0, 0, 0.18)',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          transition: 'all 0.25s cubic-bezier(0.16, 1, 0.3, 1)'
        }}
        onClick={(e) => e.stopPropagation()}
      >
        {/* Top Header Bar */}
        <div
          style={{
            padding: isFullScreen ? '16px 32px' : '18px 24px',
            borderBottom: '1px solid #e4e4e7',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: '#ffffff',
            flexShrink: 0
          }}
        >
          {/* User / Mentor Profile Identity */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
            <div style={{ position: 'relative' }}>
              <img
                src={mentor.avatarUrl}
                alt={mentor.name}
                style={{
                  width: isFullScreen ? '52px' : '44px',
                  height: isFullScreen ? '52px' : '44px',
                  borderRadius: '50%',
                  objectFit: 'cover',
                  border: '2px solid #10b981'
                }}
              />
              <span
                style={{
                  position: 'absolute',
                  bottom: '1px',
                  right: '1px',
                  width: '12px',
                  height: '12px',
                  borderRadius: '50%',
                  backgroundColor: '#10b981',
                  border: '2px solid #ffffff'
                }}
              />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px', flexWrap: 'wrap' }}>
                <h2 style={{ fontSize: isFullScreen ? '20px' : '17px', fontWeight: 800, color: '#09090b', margin: 0 }}>
                  {mentor.name}
                </h2>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 800,
                    padding: '3px 10px',
                    borderRadius: '20px',
                    backgroundColor: '#ecfdf5',
                    color: '#065f46',
                    border: '1px solid #a7f3d0',
                    display: 'inline-flex',
                    alignItems: 'center',
                    gap: '4px'
                  }}
                >
                  <ShieldCheck size={13} color="#10b981" />
                  {mentor.role === 'MENTOR' ? 'VERIFIED MENTOR' : 'LEARNER ACCOUNT'}
                </span>
                <span
                  style={{
                    fontSize: '12px',
                    fontWeight: 700,
                    color: '#71717a',
                    backgroundColor: '#f4f4f5',
                    padding: '2px 8px',
                    borderRadius: '6px'
                  }}
                >
                  ID: {mentor.loginId || mentor.id}
                </span>
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '14px', marginTop: '4px', fontSize: '12px', color: '#71717a', flexWrap: 'wrap' }}>
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Mail size={13} /> {mentor.email}
                </span>
                {mentor.phone && (
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <Phone size={13} /> {mentor.phone}
                  </span>
                )}
                {mentor.location && (
                  <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                    <MapPin size={13} /> {mentor.location}
                  </span>
                )}
                <span style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Calendar size={13} /> Member since {mentor.joinedDate}
                </span>
              </div>
            </div>
          </div>

          {/* Action Controls: Fullscreen Toggle, CSV Export, Close */}
          <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
            {/* Export CSV summary */}
            <button
              onClick={handleExportSummary}
              title="Download Growth Data (CSV)"
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 14px',
                borderRadius: '8px',
                backgroundColor: '#f4f4f5',
                border: '1px solid #e4e4e7',
                color: '#09090b',
                fontSize: '12px',
                fontWeight: 700,
                cursor: 'pointer'
              }}
            >
              <Download size={14} />
              Export CSV
            </button>

            {/* Toggle Fullscreen / Windowed Drawer */}
            <button
              onClick={() => setIsFullScreen(!isFullScreen)}
              title={isFullScreen ? 'Exit Full Screen' : 'Full Screen View'}
              style={{
                display: 'flex',
                alignItems: 'center',
                gap: '6px',
                padding: '8px 14px',
                borderRadius: '8px',
                backgroundColor: isFullScreen ? '#09090b' : '#ffffff',
                color: isFullScreen ? '#ffffff' : '#09090b',
                border: '1px solid #09090b',
                fontSize: '12px',
                fontWeight: 800,
                cursor: 'pointer'
              }}
            >
              {isFullScreen ? <Minimize2 size={14} /> : <Maximize2 size={14} />}
              {isFullScreen ? 'Exit Full Screen' : 'Full Screen'}
            </button>

            {/* Close Button */}
            <button
              onClick={onClose}
              title="Close Analytics"
              style={{
                background: '#f4f4f5',
                border: 'none',
                borderRadius: '50%',
                width: '36px',
                height: '36px',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'center',
                cursor: 'pointer',
                color: '#71717a'
              }}
            >
              <X size={18} />
            </button>
          </div>
        </div>

        {/* Scrollable Analytics Dashboard Body */}
        <div
          style={{
            padding: isFullScreen ? '28px 36px' : '20px 24px',
            overflowY: 'auto',
            flex: 1,
            backgroundColor: '#fcfcfc'
          }}
        >
          {/* Section: KPI Stats (5 cards in Full Screen) */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: isFullScreen ? 'repeat(5, 1fr)' : 'repeat(2, 1fr)',
              gap: '14px',
              marginBottom: '24px'
            }}
          >
            {/* Card 1: Total Lifetime Earned */}
            <div
              style={{
                padding: '18px 20px',
                borderRadius: '12px',
                backgroundColor: '#ffffff',
                border: '1px solid #e4e4e7',
                boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Total Lifetime Earned
                </span>
                <div
                  style={{
                    width: '30px',
                    height: '30px',
                    borderRadius: '8px',
                    backgroundColor: '#ecfdf5',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#10b981'
                  }}
                >
                  <DollarSign size={16} />
                </div>
              </div>
              <div style={{ fontSize: '26px', fontWeight: 900, color: '#09090b', marginTop: '8px' }}>
                ${mentor.earnings?.totalEarned ? mentor.earnings.totalEarned.toFixed(2) : '0.00'}
              </div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '4px', fontSize: '12px', color: '#10b981', fontWeight: 700, marginTop: '4px' }}>
                <ArrowUpRight size={15} /> +34.8% growth trend
              </div>
            </div>

            {/* Card 2: Available Balance */}
            <div
              style={{
                padding: '18px 20px',
                borderRadius: '12px',
                backgroundColor: '#ffffff',
                border: '1px solid #e4e4e7',
                boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Available Balance
                </span>
                <div
                  style={{
                    width: '30px',
                    height: '30px',
                    borderRadius: '8px',
                    backgroundColor: '#f4f4f5',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#09090b'
                  }}
                >
                  <Wallet size={16} />
                </div>
              </div>
              <div style={{ fontSize: '26px', fontWeight: 900, color: '#09090b', marginTop: '8px' }}>
                ${mentor.earnings?.balance ? mentor.earnings.balance.toFixed(2) : '0.00'}
              </div>
              <div style={{ fontSize: '12px', color: '#71717a', fontWeight: 600, marginTop: '4px' }}>
                Pending payout: ${mentor.earnings?.pendingPayout ? mentor.earnings.pendingPayout.toFixed(2) : '0.00'}
              </div>
            </div>

            {/* Card 3: Monthly Revenue Run-rate */}
            <div
              style={{
                padding: '18px 20px',
                borderRadius: '12px',
                backgroundColor: '#ffffff',
                border: '1px solid #e4e4e7',
                boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Monthly Revenue
                </span>
                <div
                  style={{
                    width: '30px',
                    height: '30px',
                    borderRadius: '8px',
                    backgroundColor: '#ecfdf5',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#10b981'
                  }}
                >
                  <TrendingUp size={16} />
                </div>
              </div>
              <div style={{ fontSize: '26px', fontWeight: 900, color: '#10b981', marginTop: '8px' }}>
                ${mentor.earnings?.monthlyRevenue ? mentor.earnings.monthlyRevenue.toFixed(2) : '0.00'}
              </div>
              <div style={{ fontSize: '12px', color: '#71717a', fontWeight: 600, marginTop: '4px' }}>
                Current month run-rate
              </div>
            </div>

            {/* Card 4: Total Students Enrolled */}
            <div
              style={{
                padding: '18px 20px',
                borderRadius: '12px',
                backgroundColor: '#ffffff',
                border: '1px solid #e4e4e7',
                boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Total Students
                </span>
                <div
                  style={{
                    width: '30px',
                    height: '30px',
                    borderRadius: '8px',
                    backgroundColor: '#f4f4f5',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#09090b'
                  }}
                >
                  <Users size={16} />
                </div>
              </div>
              <div style={{ fontSize: '26px', fontWeight: 900, color: '#09090b', marginTop: '8px' }}>
                {totalStudents}
              </div>
              <div style={{ fontSize: '12px', color: '#71717a', fontWeight: 600, marginTop: '4px' }}>
                Across {mentor.uploadsCount} published courses
              </div>
            </div>

            {/* Card 5: Average Rating */}
            <div
              style={{
                padding: '18px 20px',
                borderRadius: '12px',
                backgroundColor: '#ffffff',
                border: '1px solid #e4e4e7',
                boxShadow: '0 1px 3px rgba(0,0,0,0.04)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
                <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase', letterSpacing: '0.04em' }}>
                  Avg Rating
                </span>
                <div
                  style={{
                    width: '30px',
                    height: '30px',
                    borderRadius: '8px',
                    backgroundColor: '#fffbeb',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#f59e0b'
                  }}
                >
                  <Star size={16} />
                </div>
              </div>
              <div style={{ fontSize: '26px', fontWeight: 900, color: '#09090b', marginTop: '8px' }}>
                {avgRating} <span style={{ fontSize: '14px', color: '#71717a', fontWeight: 500 }}>/ 5.0</span>
              </div>
              <div style={{ fontSize: '12px', color: '#71717a', fontWeight: 600, marginTop: '4px' }}>
                Trust score: {mentor.trustScore}%
              </div>
            </div>
          </div>

          {/* Section: Panoramic Dual Charts (Side-by-Side in Full Screen) */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: isFullScreen ? '1.4fr 1fr' : '1fr',
              gap: '20px',
              marginBottom: '24px'
            }}
          >
            {/* Chart 1: Revenue Progression Curve (SVG Vector Graph) */}
            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '12px',
                border: '1px solid #e4e4e7',
                padding: '24px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '18px' }}>
                <div>
                  <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 800, color: '#09090b', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <TrendingUp size={18} color="#10b981" />
                    Monthly Revenue Trajectory ($ USD)
                  </h3>
                  <p style={{ margin: '3px 0 0', fontSize: '12px', color: '#71717a' }}>
                    Month-over-month course earnings progression and revenue curve
                  </p>
                </div>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <span
                    style={{
                      fontSize: '11px',
                      fontWeight: 800,
                      color: '#10b981',
                      backgroundColor: '#ecfdf5',
                      padding: '4px 10px',
                      borderRadius: '12px',
                      border: '1px solid #a7f3d0'
                    }}
                  >
                    UPWARD TREND (+34.8%)
                  </span>
                </div>
              </div>

              <div style={{ width: '100%', overflowX: 'auto' }}>
                <svg
                  width="100%"
                  height={chartHeight}
                  viewBox={`0 0 ${chartWidth} ${chartHeight}`}
                  style={{ overflow: 'visible' }}
                >
                  <defs>
                    <linearGradient id="fullScreenRevenueGrad" x1="0" y1="0" x2="0" y2="1">
                      <stop offset="0%" stopColor="#10b981" stopOpacity="0.32" />
                      <stop offset="100%" stopColor="#10b981" stopOpacity="0.0" />
                    </linearGradient>
                  </defs>

                  {/* Horizontal Grid lines */}
                  {[0, 0.25, 0.5, 0.75, 1].map((ratio, i) => {
                    const y = chartHeight - paddingY - ratio * usableHeight;
                    const label = Math.round(ratio * maxAmount);
                    return (
                      <g key={i}>
                        <line
                          x1={paddingX}
                          y1={y}
                          x2={chartWidth - paddingX}
                          y2={y}
                          stroke="#f1f5f9"
                          strokeDasharray="4 4"
                          strokeWidth="1.2"
                        />
                        <text
                          x={paddingX - 10}
                          y={y + 4}
                          fontSize="11"
                          fill="#94a3b8"
                          textAnchor="end"
                          fontWeight="700"
                        >
                          ${label}
                        </text>
                      </g>
                    );
                  })}

                  {/* Shaded Area under curve */}
                  {areaD && <path d={areaD} fill="url(#fullScreenRevenueGrad)" />}

                  {/* Line Path */}
                  {pathD && (
                    <path
                      d={pathD}
                      fill="none"
                      stroke="#10b981"
                      strokeWidth="3.5"
                      strokeLinecap="round"
                      strokeLinejoin="round"
                    />
                  )}

                  {/* Data Points and Month Labels */}
                  {points.map((pt, idx) => (
                    <g key={idx}>
                      <circle
                        cx={pt.x}
                        cy={pt.y}
                        r="6"
                        fill="#ffffff"
                        stroke="#10b981"
                        strokeWidth="3"
                      />
                      <text
                        x={pt.x}
                        y={chartHeight - 8}
                        fontSize="12"
                        fill="#475569"
                        textAnchor="middle"
                        fontWeight="800"
                      >
                        {pt.item.month}
                      </text>
                      <text
                        x={pt.x}
                        y={pt.y - 12}
                        fontSize="12"
                        fill="#09090b"
                        textAnchor="middle"
                        fontWeight="900"
                      >
                        ${pt.item.amount}
                      </text>
                    </g>
                  ))}
                </svg>
              </div>
            </div>

            {/* Chart 2: Student Enrollment Growth (Bar Chart) */}
            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '12px',
                border: '1px solid #e4e4e7',
                padding: '24px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)',
                display: 'flex',
                flexDirection: 'column',
                justifyContent: 'space-between'
              }}
            >
              <div>
                <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '6px' }}>
                  <h3 style={{ margin: 0, fontSize: '16px', fontWeight: 800, color: '#09090b', display: 'flex', alignItems: 'center', gap: '8px' }}>
                    <Users size={18} color="#09090b" />
                    Student Enrollment Growth
                  </h3>
                  <span
                    style={{
                      fontSize: '11px',
                      fontWeight: 800,
                      color: '#09090b',
                      backgroundColor: '#f4f4f5',
                      padding: '4px 10px',
                      borderRadius: '12px'
                    }}
                  >
                    {totalStudents} TOTAL
                  </span>
                </div>
                <p style={{ margin: 0, fontSize: '12px', color: '#71717a' }}>
                  Monthly registered students participating in mentor curriculum
                </p>
              </div>

              <div
                style={{
                  display: 'flex',
                  alignItems: 'flex-end',
                  justifyContent: 'space-around',
                  height: '180px',
                  paddingTop: '20px',
                  borderBottom: '2px solid #f1f5f9'
                }}
              >
                {history.map((item, idx) => {
                  const barHeight = Math.max(22, (item.students / maxStudents) * 125);
                  const isHighest = item.students === maxStudents;

                  return (
                    <div
                      key={idx}
                      style={{
                        display: 'flex',
                        flexDirection: 'column',
                        alignItems: 'center',
                        gap: '6px'
                      }}
                    >
                      <span
                        style={{
                          fontSize: '12px',
                          fontWeight: 900,
                          color: isHighest ? '#10b981' : '#09090b'
                        }}
                      >
                        {item.students}
                      </span>
                      <div
                        style={{
                          width: isFullScreen ? '46px' : '32px',
                          height: `${barHeight}px`,
                          backgroundColor: isHighest ? '#10b981' : '#09090b',
                          borderRadius: '6px 6px 0 0',
                          transition: 'all 0.3s ease',
                          boxShadow: isHighest ? '0 4px 12px rgba(16, 185, 129, 0.35)' : 'none'
                        }}
                      />
                      <span style={{ fontSize: '12px', fontWeight: 800, color: '#64748b', marginTop: '6px' }}>
                        {item.month}
                      </span>
                    </div>
                  );
                })}
              </div>

              <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', paddingTop: '10px' }}>
                <span style={{ fontSize: '11px', color: '#64748b', fontWeight: 600 }}>
                  Highest Growth: Month of {history[history.length - 1]?.month} ({maxStudents} students)
                </span>
                <span style={{ fontSize: '11px', color: '#10b981', fontWeight: 700, display: 'flex', alignItems: 'center', gap: '3px' }}>
                  <CheckCircle2 size={12} /> Positive MoM conversion
                </span>
              </div>
            </div>
          </div>

          {/* Section: Detailed Performance Ledger & Recent Sales */}
          <div
            style={{
              display: 'grid',
              gridTemplateColumns: isFullScreen ? '1fr 1fr' : '1fr',
              gap: '20px'
            }}
          >
            {/* Table 1: Month-over-Month Performance Breakdown */}
            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '12px',
                border: '1px solid #e4e4e7',
                padding: '24px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
                <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 800, color: '#09090b' }}>
                  Monthly Performance Ledger
                </h3>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a' }}>
                  6 Months Tracked
                </span>
              </div>

              <div style={{ overflowX: 'auto' }}>
                <table className="admin-table" style={{ fontSize: '13px' }}>
                  <thead>
                    <tr>
                      <th>Month</th>
                      <th>Revenue ($)</th>
                      <th>MoM Growth</th>
                      <th>Students</th>
                      <th>Rating</th>
                    </tr>
                  </thead>
                  <tbody>
                    {history.map((h, index) => {
                      const prev = index > 0 ? history[index - 1].amount : null;
                      const growth = prev ? (((h.amount - prev) / prev) * 100).toFixed(1) : null;

                      return (
                        <tr key={index}>
                          <td style={{ fontWeight: 800, color: '#09090b' }}>{h.month} 2026</td>
                          <td style={{ fontWeight: 800, color: '#10b981' }}>${h.amount.toFixed(2)}</td>
                          <td>
                            {growth ? (
                              <span
                                style={{
                                  fontSize: '11px',
                                  fontWeight: 800,
                                  color: Number(growth) >= 0 ? '#10b981' : '#ef4444',
                                  backgroundColor: Number(growth) >= 0 ? '#ecfdf5' : '#fef2f2',
                                  padding: '2px 8px',
                                  borderRadius: '6px'
                                }}
                              >
                                {Number(growth) >= 0 ? `+${growth}%` : `${growth}%`}
                              </span>
                            ) : (
                              <span style={{ color: '#a1a1aa', fontSize: '11px' }}>Baseline</span>
                            )}
                          </td>
                          <td style={{ fontWeight: 700, color: '#09090b' }}>{h.students} enrolled</td>
                          <td>
                            <span style={{ display: 'inline-flex', alignItems: 'center', gap: '3px', fontWeight: 700, color: '#f59e0b' }}>
                              ★ {h.rating}
                            </span>
                          </td>
                        </tr>
                      );
                    })}
                  </tbody>
                </table>
              </div>
            </div>

            {/* Table 2: Recent Course Sales & Learner Purchases */}
            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '12px',
                border: '1px solid #e4e4e7',
                padding: '24px',
                boxShadow: '0 1px 3px rgba(0,0,0,0.03)'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '16px' }}>
                <h3 style={{ margin: 0, fontSize: '15px', fontWeight: 800, color: '#09090b' }}>
                  Recent Student Purchases & Transactions
                </h3>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a' }}>
                  Verified Enrollment
                </span>
              </div>

              {mentor.courseSales && mentor.courseSales.length > 0 ? (
                <div style={{ display: 'flex', flexDirection: 'column', gap: '10px' }}>
                  {mentor.courseSales.map((sale) => (
                    <div
                      key={sale.id}
                      style={{
                        display: 'flex',
                        alignItems: 'center',
                        justifyContent: 'space-between',
                        padding: '12px 14px',
                        backgroundColor: '#f8fafc',
                        borderRadius: '10px',
                        border: '1px solid #e2e8f0'
                      }}
                    >
                      <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                        {sale.buyerAvatar ? (
                          <img
                            src={sale.buyerAvatar}
                            alt={sale.buyerName}
                            style={{ width: '36px', height: '36px', borderRadius: '50%', objectFit: 'cover' }}
                          />
                        ) : (
                          <div
                            style={{
                              width: '36px',
                              height: '36px',
                              borderRadius: '50%',
                              backgroundColor: '#e2e8f0',
                              display: 'flex',
                              alignItems: 'center',
                              justifyContent: 'center',
                              fontWeight: 800,
                              fontSize: '13px'
                            }}
                          >
                            {sale.buyerName.slice(0, 2)}
                          </div>
                        )}
                        <div>
                          <div style={{ fontWeight: 800, fontSize: '13px', color: '#09090b' }}>
                            {sale.buyerName}
                          </div>
                          <div style={{ fontSize: '11px', color: '#64748b' }}>
                            {sale.courseTitle} • {sale.date}
                          </div>
                        </div>
                      </div>
                      <div style={{ textAlign: 'right' }}>
                        <div style={{ fontWeight: 900, color: '#10b981', fontSize: '15px' }}>
                          +${sale.amount.toFixed(2)}
                        </div>
                        <div style={{ fontSize: '10px', color: '#94a3b8', fontFamily: 'monospace' }}>
                          {sale.transactionId}
                        </div>
                      </div>
                    </div>
                  ))}
                </div>
              ) : (
                <div style={{ fontSize: '13px', color: '#64748b', textAlign: 'center', padding: '24px' }}>
                  No recent transactions recorded for this user account.
                </div>
              )}
            </div>
          </div>

        </div>

        {/* Footer Bar in Full Screen */}
        <div
          style={{
            padding: isFullScreen ? '14px 32px' : '14px 24px',
            borderTop: '1px solid #e4e4e7',
            backgroundColor: '#ffffff',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            flexShrink: 0
          }}
        >
          <div style={{ fontSize: '12px', color: '#71717a', fontWeight: 600 }}>
            Showing complete Growth Analytics for <strong style={{ color: '#09090b' }}>{mentor.name}</strong>
          </div>
          <button
            onClick={onClose}
            style={{
              backgroundColor: '#09090b',
              color: '#ffffff',
              border: 'none',
              padding: '9px 24px',
              borderRadius: '8px',
              fontSize: '13px',
              fontWeight: 800,
              cursor: 'pointer'
            }}
          >
            Close Analytics View
          </button>
        </div>

      </div>
    </div>
  );
};
