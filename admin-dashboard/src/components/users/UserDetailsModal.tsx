import React from 'react';
import { AdminUser } from '../../types/admin';
import {
  X,
  DollarSign,
  TrendingUp,
  ShoppingBag,
  ShieldAlert,
  AlertTriangle,
  Ban,
  CheckCircle2,
  Phone,
  Mail,
  MapPin,
  Calendar,
  Clock,
  User
} from 'lucide-react';

interface UserDetailsModalProps {
  user: AdminUser | null;
  isOpen: boolean;
  onClose: () => void;
  onOpenAnalytics?: (mentor: AdminUser) => void;
  onModerateUser?: (user: AdminUser, action: 'BLOCK' | 'SUSPEND' | 'WARN' | 'ACTIVE') => void;
}

export const UserDetailsModal: React.FC<UserDetailsModalProps> = ({
  user,
  isOpen,
  onClose,
  onOpenAnalytics,
  onModerateUser
}) => {
  if (!isOpen || !user) return null;

  const isBlocked = user.status === 'BLOCKED';
  const isSuspended = user.status === 'SUSPENDED';
  const isWarned = user.status === 'WARNED';
  const isMentor = user.role === 'MENTOR';

  return (
    <div className="modal-overlay" onClick={onClose} style={{ zIndex: 1050 }}>
      <div
        className="modal-container"
        onClick={(e) => e.stopPropagation()}
        style={{
          maxWidth: '820px',
          width: '95%',
          maxHeight: '90vh',
          display: 'flex',
          flexDirection: 'column',
          overflow: 'hidden',
          padding: 0
        }}
      >
        {/* Modal Header */}
        <div
          style={{
            padding: '20px 24px',
            borderBottom: '1px solid #e4e4e7',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'space-between',
            backgroundColor: '#ffffff'
          }}
        >
          <div style={{ display: 'flex', alignItems: 'center', gap: '14px' }}>
            <div style={{ position: 'relative' }}>
              <img
                src={user.avatarUrl}
                alt={user.name}
                style={{
                  width: '52px',
                  height: '52px',
                  borderRadius: '50%',
                  objectFit: 'cover',
                  border: '2px solid #e4e4e7'
                }}
              />
              <span
                style={{
                  position: 'absolute',
                  bottom: -2,
                  right: -2,
                  width: '14px',
                  height: '14px',
                  borderRadius: '50%',
                  backgroundColor: isBlocked ? '#ef4444' : isWarned ? '#f59e0b' : '#10b981',
                  border: '2px solid #ffffff'
                }}
              />
            </div>
            <div>
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <h3 style={{ fontSize: '18px', fontWeight: 800, color: '#09090b', margin: 0 }}>
                  {user.name}
                </h3>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 700,
                    padding: '2px 8px',
                    borderRadius: '20px',
                    backgroundColor: isMentor ? '#09090b' : '#f4f4f5',
                    color: isMentor ? '#ffffff' : '#09090b',
                    letterSpacing: '0.04em'
                  }}
                >
                  {user.role}
                </span>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 700,
                    padding: '2px 8px',
                    borderRadius: '20px',
                    backgroundColor: isBlocked
                      ? '#fef2f2'
                      : isSuspended
                      ? '#fffbeb'
                      : isWarned
                      ? '#fefce8'
                      : '#ecfdf5',
                    color: isBlocked
                      ? '#ef4444'
                      : isSuspended
                      ? '#b45309'
                      : isWarned
                      ? '#854d0e'
                      : '#10b981',
                    border: `1px solid ${
                      isBlocked ? '#fecaca' : isSuspended || isWarned ? '#fef08a' : '#a7f3d0'
                    }`
                  }}
                >
                  {user.status}
                </span>
              </div>
              <p style={{ fontSize: '13px', color: '#71717a', margin: '2px 0 0' }}>
                User ID: <code style={{ color: '#09090b', fontWeight: 600 }}>{user.id}</code> • Trust Score: <strong style={{ color: user.trustScore < 50 ? '#ef4444' : '#10b981' }}>{user.trustScore}%</strong>
              </p>
            </div>
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
            {isMentor && onOpenAnalytics && (
              <button
                onClick={() => {
                  onClose();
                  onOpenAnalytics(user);
                }}
                className="btn"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  backgroundColor: '#ecfdf5',
                  color: '#065f46',
                  border: '1px solid #a7f3d0',
                  padding: '7px 12px',
                  borderRadius: '8px',
                  fontSize: '12px',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                <TrendingUp size={14} color="#10b981" />
                Growth Analytics
              </button>
            )}
            <button
              onClick={onClose}
              style={{
                background: 'transparent',
                border: 'none',
                cursor: 'pointer',
                color: '#71717a',
                padding: '6px',
                borderRadius: '6px'
              }}
            >
              <X size={20} />
            </button>
          </div>
        </div>

        {/* Modal Scrollable Body */}
        <div style={{ padding: '24px', overflowY: 'auto', flex: 1, backgroundColor: '#fafafa' }}>
          {/* Security Alert if blocked or warned */}
          {(isBlocked || isSuspended || isWarned) && (
            <div
              style={{
                marginBottom: '20px',
                padding: '14px 16px',
                borderRadius: '10px',
                backgroundColor: isBlocked ? '#fef2f2' : '#fffbeb',
                border: `1px solid ${isBlocked ? '#fecaca' : '#fed7aa'}`,
                display: 'flex',
                alignItems: 'flex-start',
                gap: '12px'
              }}
            >
              <ShieldAlert size={20} color={isBlocked ? '#ef4444' : '#f59e0b'} style={{ marginTop: '2px', flexShrink: 0 }} />
              <div>
                <div style={{ fontWeight: 800, fontSize: '13px', color: isBlocked ? '#991b1b' : '#92400e' }}>
                  Moderation Notice: {user.status} (Warnings: {user.warningCount})
                </div>
                <div style={{ fontSize: '12px', color: '#4b5563', marginTop: '2px' }}>
                  {user.reasonBlocked || 'Account under administrative scrutiny due to platform guideline violation.'}
                </div>
              </div>
            </div>
          )}

          {/* Section 1: Personal & Contact Information */}
          <div
            style={{
              backgroundColor: '#ffffff',
              borderRadius: '12px',
              border: '1px solid #e4e4e7',
              padding: '20px',
              marginBottom: '20px'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
              <User size={18} color="#09090b" />
              <h4 style={{ margin: 0, fontSize: '14px', fontWeight: 800, color: '#09090b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Personal Details
              </h4>
            </div>

            <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px' }}>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase' }}>Full Name</span>
                <div style={{ fontSize: '14px', fontWeight: 700, color: '#09090b', marginTop: '2px' }}>{user.name}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Mail size={12} /> Email Address
                </span>
                <div style={{ fontSize: '14px', fontWeight: 600, color: '#09090b', marginTop: '2px' }}>{user.email}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Phone size={12} /> Phone Number
                </span>
                <div style={{ fontSize: '14px', fontWeight: 600, color: '#09090b', marginTop: '2px' }}>{user.phone || '+91 98765 00000'}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <MapPin size={12} /> Location
                </span>
                <div style={{ fontSize: '14px', fontWeight: 600, color: '#09090b', marginTop: '2px' }}>{user.location || 'India'}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Calendar size={12} /> Registered Date
                </span>
                <div style={{ fontSize: '14px', fontWeight: 600, color: '#09090b', marginTop: '2px' }}>{user.joinedDate}</div>
              </div>
              <div>
                <span style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', display: 'flex', alignItems: 'center', gap: '4px' }}>
                  <Clock size={12} /> Last Active
                </span>
                <div style={{ fontSize: '14px', fontWeight: 600, color: '#09090b', marginTop: '2px' }}>{user.lastActive}</div>
              </div>
            </div>
          </div>

          {/* Section 3: Financials & Earnings (For Mentors) or Purchase History (For Learners) */}
          {isMentor ? (
            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '12px',
                border: '1px solid #e4e4e7',
                padding: '20px',
                marginBottom: '20px'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '14px' }}>
                <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                  <DollarSign size={18} color="#10b981" />
                  <h4 style={{ margin: 0, fontSize: '14px', fontWeight: 800, color: '#09090b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                    Mentor Earnings & Sales Summary
                  </h4>
                </div>
                {onOpenAnalytics && (
                  <button
                    onClick={() => {
                      onClose();
                      onOpenAnalytics(user);
                    }}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#10b981',
                      fontSize: '12px',
                      fontWeight: 800,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '4px'
                    }}
                  >
                    View Growth Chart &rarr;
                  </button>
                )}
              </div>

              {/* 4 Earning KPI Cards */}
              <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(160px, 1fr))', gap: '12px', marginBottom: '18px' }}>
                <div style={{ padding: '12px', borderRadius: '8px', backgroundColor: '#f0fdf4', border: '1px solid #bbf7d0' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: '#166534', textTransform: 'uppercase' }}>Total Earned</span>
                  <div style={{ fontSize: '20px', fontWeight: 900, color: '#10b981', marginTop: '4px' }}>
                    ${user.earnings?.totalEarned?.toFixed(2) || '0.00'}
                  </div>
                </div>
                <div style={{ padding: '12px', borderRadius: '8px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: '#475569', textTransform: 'uppercase' }}>Available Balance</span>
                  <div style={{ fontSize: '20px', fontWeight: 900, color: '#09090b', marginTop: '4px' }}>
                    ${user.earnings?.balance?.toFixed(2) || '0.00'}
                  </div>
                </div>
                <div style={{ padding: '12px', borderRadius: '8px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: '#475569', textTransform: 'uppercase' }}>This Month Rev</span>
                  <div style={{ fontSize: '20px', fontWeight: 900, color: '#09090b', marginTop: '4px' }}>
                    ${user.earnings?.monthlyRevenue?.toFixed(2) || '0.00'}
                  </div>
                </div>
                <div style={{ padding: '12px', borderRadius: '8px', backgroundColor: '#f8fafc', border: '1px solid #e2e8f0' }}>
                  <span style={{ fontSize: '11px', fontWeight: 700, color: '#475569', textTransform: 'uppercase' }}>Pending Payout</span>
                  <div style={{ fontSize: '20px', fontWeight: 900, color: '#f59e0b', marginTop: '4px' }}>
                    ${user.earnings?.pendingPayout?.toFixed(2) || '0.00'}
                  </div>
                </div>
              </div>

              {/* Who Purchased Section */}
              <div>
                <h5 style={{ fontSize: '12px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase', margin: '0 0 10px', letterSpacing: '0.04em' }}>
                  Who Purchased (Recent Learners & Transactions)
                </h5>
                {user.courseSales && user.courseSales.length > 0 ? (
                  <div style={{ border: '1px solid #e4e4e7', borderRadius: '8px', overflow: 'hidden' }}>
                    <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                      <thead>
                        <tr style={{ backgroundColor: '#f4f4f5', borderBottom: '1px solid #e4e4e7', textAlign: 'left' }}>
                          <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Learner</th>
                          <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Course Title</th>
                          <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Amount</th>
                          <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Date</th>
                          <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Txn ID</th>
                        </tr>
                      </thead>
                      <tbody>
                        {user.courseSales.map((sale) => (
                          <tr key={sale.id} style={{ borderBottom: '1px solid #f4f4f5', backgroundColor: '#ffffff' }}>
                            <td style={{ padding: '10px 14px' }}>
                              <div style={{ fontWeight: 700, color: '#09090b' }}>{sale.buyerName}</div>
                              <div style={{ fontSize: '11px', color: '#71717a' }}>{sale.buyerEmail}</div>
                            </td>
                            <td style={{ padding: '10px 14px', fontWeight: 600, color: '#09090b' }}>
                              {sale.courseTitle}
                            </td>
                            <td style={{ padding: '10px 14px', fontWeight: 800, color: '#10b981' }}>
                              ${sale.amount.toFixed(2)}
                            </td>
                            <td style={{ padding: '10px 14px', color: '#71717a' }}>{sale.date}</td>
                            <td style={{ padding: '10px 14px' }}>
                              <code style={{ fontSize: '11px', color: '#64748b' }}>{sale.transactionId}</code>
                            </td>
                          </tr>
                        ))}
                      </tbody>
                    </table>
                  </div>
                ) : (
                  <div style={{ padding: '18px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', color: '#64748b', fontSize: '13px' }}>
                    No direct sales recorded for this mentor yet.
                  </div>
                )}
              </div>
            </div>
          ) : (
            /* Learner Purchased Courses */
            <div
              style={{
                backgroundColor: '#ffffff',
                borderRadius: '12px',
                border: '1px solid #e4e4e7',
                padding: '20px',
                marginBottom: '20px'
              }}
            >
              <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '14px' }}>
                <ShoppingBag size={18} color="#09090b" />
                <h4 style={{ margin: 0, fontSize: '14px', fontWeight: 800, color: '#09090b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                  Courses Purchased & Enrolled
                </h4>
              </div>

              {user.purchasedCourses && user.purchasedCourses.length > 0 ? (
                <div style={{ border: '1px solid #e4e4e7', borderRadius: '8px', overflow: 'hidden' }}>
                  <table style={{ width: '100%', borderCollapse: 'collapse', fontSize: '13px' }}>
                    <thead>
                      <tr style={{ backgroundColor: '#f4f4f5', borderBottom: '1px solid #e4e4e7', textAlign: 'left' }}>
                        <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Course</th>
                        <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Mentor</th>
                        <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Price</th>
                        <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Date</th>
                        <th style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>Status</th>
                      </tr>
                    </thead>
                    <tbody>
                      {user.purchasedCourses.map((pur) => (
                        <tr key={pur.id} style={{ borderBottom: '1px solid #f4f4f5', backgroundColor: '#ffffff' }}>
                          <td style={{ padding: '10px 14px', fontWeight: 700, color: '#09090b' }}>
                            {pur.courseTitle}
                          </td>
                          <td style={{ padding: '10px 14px', color: '#09090b', fontWeight: 500 }}>
                            {pur.mentorName}
                          </td>
                          <td style={{ padding: '10px 14px', fontWeight: 800, color: '#09090b' }}>
                            ${pur.amount.toFixed(2)}
                          </td>
                          <td style={{ padding: '10px 14px', color: '#71717a' }}>{pur.date}</td>
                          <td style={{ padding: '10px 14px' }}>
                            <span
                              style={{
                                fontSize: '11px',
                                fontWeight: 700,
                                padding: '2px 8px',
                                borderRadius: '12px',
                                backgroundColor: pur.status === 'ACTIVE' ? '#ecfdf5' : '#fef2f2',
                                color: pur.status === 'ACTIVE' ? '#10b981' : '#ef4444'
                              }}
                            >
                              {pur.status}
                            </span>
                          </td>
                        </tr>
                      ))}
                    </tbody>
                  </table>
                </div>
              ) : (
                <div style={{ padding: '18px', textAlign: 'center', backgroundColor: '#f8fafc', borderRadius: '8px', color: '#64748b', fontSize: '13px' }}>
                  No course purchases recorded for this learner yet.
                </div>
              )}
            </div>
          )}

          {/* Section 4: Moderation Actions (Block, Suspend, Warning, Unblock) */}
          <div
            style={{
              backgroundColor: '#ffffff',
              borderRadius: '12px',
              border: '1px solid #e4e4e7',
              padding: '20px'
            }}
          >
            <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginBottom: '12px' }}>
              <ShieldAlert size={18} color="#ef4444" />
              <h4 style={{ margin: 0, fontSize: '14px', fontWeight: 800, color: '#09090b', textTransform: 'uppercase', letterSpacing: '0.05em' }}>
                Administrative Moderation Controls
              </h4>
            </div>
            <p style={{ fontSize: '12px', color: '#64748b', margin: '0 0 16px' }}>
              Instantly adjust user access permissions, issue formal warnings, or terminate bad practice accounts across the mobile app and platform.
            </p>

            <div style={{ display: 'flex', flexWrap: 'wrap', gap: '10px' }}>
              {/* Warning Option */}
              <button
                onClick={() => onModerateUser && onModerateUser(user, 'WARN')}
                className="btn"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  backgroundColor: '#fffbeb',
                  color: '#b45309',
                  border: '1px solid #fcd34d',
                  padding: '9px 16px',
                  borderRadius: '8px',
                  fontSize: '13px',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                <AlertTriangle size={16} />
                Send Warning ({user.warningCount} issued)
              </button>

              {/* Suspend Option */}
              <button
                onClick={() => onModerateUser && onModerateUser(user, 'SUSPEND')}
                className="btn"
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  gap: '6px',
                  backgroundColor: '#fff7ed',
                  color: '#c2410c',
                  border: '1px solid #fdba74',
                  padding: '9px 16px',
                  borderRadius: '8px',
                  fontSize: '13px',
                  fontWeight: 700,
                  cursor: 'pointer'
                }}
              >
                <Clock size={16} />
                Suspend Account (7 Days)
              </button>

              {/* Block / Ban Option */}
              {isBlocked || isSuspended ? (
                <button
                  onClick={() => onModerateUser && onModerateUser(user, 'ACTIVE')}
                  className="btn"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    backgroundColor: '#10b981',
                    color: '#ffffff',
                    border: 'none',
                    padding: '9px 16px',
                    borderRadius: '8px',
                    fontSize: '13px',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  <CheckCircle2 size={16} />
                  Restore & Unblock User
                </button>
              ) : (
                <button
                  onClick={() => onModerateUser && onModerateUser(user, 'BLOCK')}
                  className="btn"
                  style={{
                    display: 'flex',
                    alignItems: 'center',
                    gap: '6px',
                    backgroundColor: '#ef4444',
                    color: '#ffffff',
                    border: 'none',
                    padding: '9px 16px',
                    borderRadius: '8px',
                    fontSize: '13px',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  <Ban size={16} />
                  Block Account
                </button>
              )}
            </div>
          </div>
        </div>

        {/* Modal Footer */}
        <div
          style={{
            padding: '16px 24px',
            borderTop: '1px solid #e4e4e7',
            backgroundColor: '#ffffff',
            display: 'flex',
            justifyContent: 'flex-end'
          }}
        >
          <button
            onClick={onClose}
            className="btn"
            style={{
              backgroundColor: '#09090b',
              color: '#ffffff',
              border: 'none',
              padding: '8px 20px',
              borderRadius: '8px',
              fontSize: '13px',
              fontWeight: 700,
              cursor: 'pointer'
            }}
          >
            Close Details
          </button>
        </div>
      </div>
    </div>
  );
};
