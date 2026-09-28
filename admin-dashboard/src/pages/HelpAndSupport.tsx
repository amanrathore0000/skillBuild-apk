import React, { useState, useMemo } from 'react';
import { SupportComplaint, ComplaintCategory, ComplaintStatus, AdminUser } from '../types/admin';
import {
  LifeBuoy,
  Search,
  Filter,
  CheckCircle2,
  Clock,
  AlertCircle,
  MessageSquare,
  User,
  Phone,
  Mail,
  X,
  Send,
  ShieldAlert,
  ChevronRight,
  HelpCircle,
  DollarSign,
  Video,
  FileText
} from 'lucide-react';

interface HelpAndSupportProps {
  complaints: SupportComplaint[];
  users: AdminUser[];
  onUpdateComplaintStatus: (id: string, status: ComplaintStatus, resolutionNote?: string) => void;
  onOpenBlockModal?: (user: AdminUser) => void;
}

export const HelpAndSupport: React.FC<HelpAndSupportProps> = ({
  complaints,
  users,
  onUpdateComplaintStatus,
  onOpenBlockModal
}) => {
  const [search, setSearch] = useState('');
  const [statusFilter, setStatusFilter] = useState<'ALL' | ComplaintStatus>('ALL');
  const [categoryFilter, setCategoryFilter] = useState<'ALL' | ComplaintCategory>('ALL');
  const [selectedComplaint, setSelectedComplaint] = useState<SupportComplaint | null>(null);
  const [resolutionInput, setResolutionInput] = useState('');

  const stats = useMemo(() => {
    return {
      total: complaints.length,
      pending: complaints.filter(c => c.status === 'PENDING').length,
      inReview: complaints.filter(c => c.status === 'IN_REVIEW').length,
      resolved: complaints.filter(c => c.status === 'RESOLVED').length,
      critical: complaints.filter(c => c.priority === 'CRITICAL').length
    };
  }, [complaints]);

  const filteredComplaints = useMemo(() => {
    return complaints.filter(c => {
      const q = search.toLowerCase().trim();
      const matchesSearch =
        !q ||
        c.userName.toLowerCase().includes(q) ||
        c.userEmail.toLowerCase().includes(q) ||
        c.subject.toLowerCase().includes(q) ||
        c.description.toLowerCase().includes(q) ||
        c.id.toLowerCase().includes(q);

      const matchesStatus = statusFilter === 'ALL' || c.status === statusFilter;
      const matchesCategory = categoryFilter === 'ALL' || c.category === categoryFilter;

      return matchesSearch && matchesStatus && matchesCategory;
    });
  }, [complaints, search, statusFilter, categoryFilter]);

  const handleResolve = () => {
    if (!selectedComplaint) return;
    onUpdateComplaintStatus(selectedComplaint.id, 'RESOLVED', resolutionInput || 'Resolved by Administrator');
    setSelectedComplaint(prev => prev ? { ...prev, status: 'RESOLVED', resolutionNote: resolutionInput || 'Resolved by Administrator' } : null);
    setResolutionInput('');
  };

  const handleSetInReview = () => {
    if (!selectedComplaint) return;
    onUpdateComplaintStatus(selectedComplaint.id, 'IN_REVIEW', 'Under active investigation');
    setSelectedComplaint(prev => prev ? { ...prev, status: 'IN_REVIEW' } : null);
  };

  const handleDismiss = () => {
    if (!selectedComplaint) return;
    onUpdateComplaintStatus(selectedComplaint.id, 'DISMISSED', 'Closed without action');
    setSelectedComplaint(prev => prev ? { ...prev, status: 'DISMISSED', resolutionNote: 'Closed without action' } : null);
  };

  const getCategoryIcon = (cat: ComplaintCategory) => {
    switch (cat) {
      case 'PAYMENT_ISSUE':
        return <DollarSign size={14} color="#10b981" />;
      case 'VIDEO_STREAM_BUG':
        return <Video size={14} color="#ef4444" />;
      case 'MENTOR_DISPUTE':
        return <ShieldAlert size={14} color="#ef4444" />;
      default:
        return <FileText size={14} color="#71717a" />;
    }
  };

  return (
    <div>
      {/* Header Metric Cards */}
      <div style={{ display: 'grid', gridTemplateColumns: 'repeat(auto-fit, minmax(220px, 1fr))', gap: '16px', marginBottom: '24px' }}>
        <div className="glass-card" style={{ padding: '18px 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase' }}>
              Pending Action
            </span>
            <div style={{ width: '28px', height: '28px', borderRadius: '6px', backgroundColor: '#fef2f2', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#ef4444' }}>
              <AlertCircle size={16} />
            </div>
          </div>
          <div style={{ fontSize: '26px', fontWeight: 900, color: '#ef4444', marginTop: '6px' }}>
            {stats.pending}
          </div>
          <div style={{ fontSize: '11px', color: '#71717a', fontWeight: 600, marginTop: '2px' }}>
            Complaints awaiting admin triage
          </div>
        </div>

        <div className="glass-card" style={{ padding: '18px 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase' }}>
              In Review
            </span>
            <div style={{ width: '28px', height: '28px', borderRadius: '6px', backgroundColor: '#fffbeb', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#b45309' }}>
              <Clock size={16} />
            </div>
          </div>
          <div style={{ fontSize: '26px', fontWeight: 900, color: '#09090b', marginTop: '6px' }}>
            {stats.inReview}
          </div>
          <div style={{ fontSize: '11px', color: '#71717a', fontWeight: 600, marginTop: '2px' }}>
            Currently being investigated
          </div>
        </div>

        <div className="glass-card" style={{ padding: '18px 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase' }}>
              Resolved Tickets
            </span>
            <div style={{ width: '28px', height: '28px', borderRadius: '6px', backgroundColor: '#ecfdf5', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#10b981' }}>
              <CheckCircle2 size={16} />
            </div>
          </div>
          <div style={{ fontSize: '26px', fontWeight: 900, color: '#10b981', marginTop: '6px' }}>
            {stats.resolved}
          </div>
          <div style={{ fontSize: '11px', color: '#71717a', fontWeight: 600, marginTop: '2px' }}>
            Successfully closed issues
          </div>
        </div>

        <div className="glass-card" style={{ padding: '18px 20px' }}>
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between' }}>
            <span style={{ fontSize: '11px', fontWeight: 800, color: '#71717a', textTransform: 'uppercase' }}>
              Critical Issues
            </span>
            <div style={{ width: '28px', height: '28px', borderRadius: '6px', backgroundColor: '#fef2f2', display: 'flex', alignItems: 'center', justifyContent: 'center', color: '#ef4444' }}>
              <ShieldAlert size={16} />
            </div>
          </div>
          <div style={{ fontSize: '26px', fontWeight: 900, color: '#ef4444', marginTop: '6px' }}>
            {stats.critical}
          </div>
          <div style={{ fontSize: '11px', color: '#71717a', fontWeight: 600, marginTop: '2px' }}>
            High priority / dispute tickets
          </div>
        </div>
      </div>

      {/* Filter & Search Bar */}
      <div className="glass-card" style={{ padding: '18px 24px', marginBottom: '24px' }}>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', flexWrap: 'wrap', gap: '16px' }}>
          <div className="search-input-wrapper" style={{ flex: 1, minWidth: '300px', maxWidth: '480px' }}>
            <Search size={16} />
            <input
              type="text"
              className="search-input"
              placeholder="Search complaints by user, subject, description, ticket ID..."
              value={search}
              onChange={(e) => setSearch(e.target.value)}
            />
            {search && (
              <button
                onClick={() => setSearch('')}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#71717a', padding: 0 }}
              >
                <X size={14} />
              </button>
            )}
          </div>

          <div style={{ display: 'flex', alignItems: 'center', gap: '12px', flexWrap: 'wrap' }}>
            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '12px', color: '#71717a', fontWeight: 700, textTransform: 'uppercase' }}>
                Status:
              </span>
              <select
                className="select-input"
                value={statusFilter}
                onChange={(e) => setStatusFilter(e.target.value as any)}
                style={{ fontWeight: 700 }}
              >
                <option value="ALL">All Statuses ({complaints.length})</option>
                <option value="PENDING">Pending ({stats.pending})</option>
                <option value="IN_REVIEW">In Review ({stats.inReview})</option>
                <option value="RESOLVED">Resolved ({stats.resolved})</option>
                <option value="DISMISSED">Dismissed</option>
              </select>
            </div>

            <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
              <span style={{ fontSize: '12px', color: '#71717a', fontWeight: 700, textTransform: 'uppercase' }}>
                Category:
              </span>
              <select
                className="select-input"
                value={categoryFilter}
                onChange={(e) => setCategoryFilter(e.target.value as any)}
                style={{ fontWeight: 700 }}
              >
                <option value="ALL">All Categories</option>
                <option value="PAYMENT_ISSUE">Payment / Billing</option>
                <option value="VIDEO_STREAM_BUG">Video Stream / Bug</option>
                <option value="MENTOR_DISPUTE">Mentor Dispute</option>
                <option value="ACCOUNT_ACCESS">Account Access</option>
                <option value="CONTENT_VIOLATION">Content Violation</option>
                <option value="OTHER">Other</option>
              </select>
            </div>
          </div>
        </div>
      </div>

      {/* Complaints Table */}
      <div className="glass-card" style={{ overflow: 'hidden' }}>
        <div className="table-wrapper">
          <table className="admin-table">
            <thead>
              <tr>
                <th>Ticket ID / Time</th>
                <th>User / Reporter</th>
                <th>Category</th>
                <th>Complaint Subject & Details</th>
                <th>Priority</th>
                <th>Status</th>
                <th style={{ textAlign: 'right' }}>Actions</th>
              </tr>
            </thead>
            <tbody>
              {filteredComplaints.length === 0 ? (
                <tr>
                  <td colSpan={7} style={{ textAlign: 'center', padding: '48px', color: '#71717a', fontWeight: 600 }}>
                    No support complaints found matching current filters.
                  </td>
                </tr>
              ) : (
                filteredComplaints.map(item => {
                  const isPending = item.status === 'PENDING';
                  const isResolved = item.status === 'RESOLVED';
                  const isCritical = item.priority === 'CRITICAL';

                  return (
                    <tr
                      key={item.id}
                      style={{
                        background: isCritical && isPending ? '#fef2f2' : undefined,
                        cursor: 'pointer'
                      }}
                      onClick={() => setSelectedComplaint(item)}
                    >
                      {/* ID & Timestamp */}
                      <td>
                        <code style={{ fontSize: '11px', fontWeight: 800, color: '#09090b', backgroundColor: '#f4f4f5', padding: '2px 6px', borderRadius: '4px' }}>
                          {item.id}
                        </code>
                        <div style={{ fontSize: '11px', color: '#71717a', marginTop: '3px' }}>
                          {item.timestamp}
                        </div>
                      </td>

                      {/* User / Reporter */}
                      <td>
                        <div style={{ fontWeight: 800, fontSize: '13px', color: '#09090b' }}>
                          {item.userName}
                        </div>
                        <div style={{ fontSize: '11px', color: '#71717a' }}>
                          {item.userEmail}
                        </div>
                        <span
                          style={{
                            fontSize: '9px',
                            fontWeight: 800,
                            padding: '1px 6px',
                            borderRadius: '10px',
                            backgroundColor: item.userRole === 'MENTOR' ? '#09090b' : '#f4f4f5',
                            color: item.userRole === 'MENTOR' ? '#ffffff' : '#09090b',
                            marginTop: '2px',
                            display: 'inline-block'
                          }}
                        >
                          {item.userRole}
                        </span>
                      </td>

                      {/* Category */}
                      <td>
                        <div style={{ display: 'inline-flex', alignItems: 'center', gap: '6px', fontSize: '12px', fontWeight: 700, color: '#09090b' }}>
                          {getCategoryIcon(item.category)}
                          {item.category.replace(/_/g, ' ')}
                        </div>
                      </td>

                      {/* Subject & Description */}
                      <td style={{ maxWidth: '340px' }}>
                        <div style={{ fontWeight: 800, fontSize: '13px', color: '#09090b' }}>
                          {item.subject}
                        </div>
                        <div
                          style={{
                            fontSize: '12px',
                            color: '#71717a',
                            marginTop: '2px',
                            whiteSpace: 'nowrap',
                            overflow: 'hidden',
                            textOverflow: 'ellipsis'
                          }}
                        >
                          {item.description}
                        </div>
                      </td>

                      {/* Priority */}
                      <td>
                        <span
                          style={{
                            fontSize: '11px',
                            fontWeight: 800,
                            padding: '2px 8px',
                            borderRadius: '10px',
                            backgroundColor:
                              item.priority === 'CRITICAL' ? '#fef2f2' :
                              item.priority === 'HIGH' ? '#fff7ed' :
                              item.priority === 'MEDIUM' ? '#fffbeb' : '#f4f4f5',
                            color:
                              item.priority === 'CRITICAL' ? '#ef4444' :
                              item.priority === 'HIGH' ? '#c2410c' :
                              item.priority === 'MEDIUM' ? '#b45309' : '#71717a',
                            border: `1px solid ${
                              item.priority === 'CRITICAL' ? '#fecaca' :
                              item.priority === 'HIGH' ? '#fed7aa' :
                              item.priority === 'MEDIUM' ? '#fde68a' : '#e4e4e7'
                            }`
                          }}
                        >
                          {item.priority}
                        </span>
                      </td>

                      {/* Status */}
                      <td>
                        <span
                          style={{
                            fontSize: '11px',
                            fontWeight: 800,
                            padding: '3px 8px',
                            borderRadius: '12px',
                            backgroundColor:
                              item.status === 'RESOLVED' ? '#ecfdf5' :
                              item.status === 'IN_REVIEW' ? '#fffbeb' :
                              item.status === 'PENDING' ? '#fef2f2' : '#f4f4f5',
                            color:
                              item.status === 'RESOLVED' ? '#10b981' :
                              item.status === 'IN_REVIEW' ? '#b45309' :
                              item.status === 'PENDING' ? '#ef4444' : '#71717a',
                            border: `1px solid ${
                              item.status === 'RESOLVED' ? '#a7f3d0' :
                              item.status === 'IN_REVIEW' ? '#fde68a' :
                              item.status === 'PENDING' ? '#fecaca' : '#e4e4e7'
                            }`
                          }}
                        >
                          {item.status}
                        </span>
                      </td>

                      {/* Action */}
                      <td style={{ textAlign: 'right' }}>
                        <button
                          className="btn btn-outline btn-sm"
                          onClick={(e) => {
                            e.stopPropagation();
                            setSelectedComplaint(item);
                          }}
                          style={{ padding: '5px 10px', fontSize: '11px', fontWeight: 700 }}
                        >
                          Review Ticket
                        </button>
                      </td>
                    </tr>
                  );
                })
              )}
            </tbody>
          </table>
        </div>
      </div>

      {/* Ticket Review Modal */}
      {selectedComplaint && (
        <div className="modal-overlay" onClick={() => setSelectedComplaint(null)}>
          <div
            className="modal-container"
            style={{ maxWidth: '680px' }}
            onClick={(e) => e.stopPropagation()}
          >
            {/* Header */}
            <div className="modal-header">
              <div style={{ display: 'flex', alignItems: 'center', gap: '10px' }}>
                <div
                  style={{
                    width: '38px',
                    height: '38px',
                    borderRadius: '8px',
                    backgroundColor: '#f4f4f5',
                    display: 'flex',
                    alignItems: 'center',
                    justifyContent: 'center',
                    color: '#09090b'
                  }}
                >
                  <LifeBuoy size={20} />
                </div>
                <div>
                  <h3 style={{ fontSize: '16px', fontWeight: 800, color: '#09090b', margin: 0 }}>
                    Support Ticket: {selectedComplaint.id}
                  </h3>
                  <p style={{ fontSize: '12px', color: '#71717a', margin: '2px 0 0' }}>
                    Submitted via SkillBuilder Mobile App • {selectedComplaint.timestamp}
                  </p>
                </div>
              </div>
              <button
                onClick={() => setSelectedComplaint(null)}
                style={{ background: 'none', border: 'none', cursor: 'pointer', color: '#71717a' }}
              >
                <X size={20} />
              </button>
            </div>

            {/* Modal Body */}
            <div style={{ padding: '20px 24px', maxHeight: '70vh', overflowY: 'auto' }}>
              {/* User info snippet */}
              <div
                style={{
                  padding: '12px 16px',
                  backgroundColor: '#f8fafc',
                  border: '1px solid #e2e8f0',
                  borderRadius: '8px',
                  marginBottom: '16px',
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  flexWrap: 'wrap',
                  gap: '10px'
                }}
              >
                <div>
                  <div style={{ fontWeight: 800, fontSize: '14px', color: '#09090b' }}>
                    {selectedComplaint.userName} ({selectedComplaint.userRole})
                  </div>
                  <div style={{ fontSize: '12px', color: '#64748b', marginTop: '2px' }}>
                    {selectedComplaint.userEmail} {selectedComplaint.userPhone && `• ${selectedComplaint.userPhone}`}
                  </div>
                </div>
                <span
                  style={{
                    fontSize: '11px',
                    fontWeight: 800,
                    padding: '3px 10px',
                    borderRadius: '12px',
                    backgroundColor: selectedComplaint.status === 'RESOLVED' ? '#ecfdf5' : '#fef2f2',
                    color: selectedComplaint.status === 'RESOLVED' ? '#10b981' : '#ef4444'
                  }}
                >
                  {selectedComplaint.status}
                </span>
              </div>

              {/* Subject & Description */}
              <div style={{ marginBottom: '20px' }}>
                <div style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', marginBottom: '4px' }}>
                  Issue Subject
                </div>
                <div style={{ fontSize: '16px', fontWeight: 800, color: '#09090b' }}>
                  {selectedComplaint.subject}
                </div>
              </div>

              <div style={{ marginBottom: '20px' }}>
                <div style={{ fontSize: '11px', fontWeight: 700, color: '#71717a', textTransform: 'uppercase', marginBottom: '4px' }}>
                  Complaint Description
                </div>
                <div
                  style={{
                    backgroundColor: '#ffffff',
                    border: '1px solid #e4e4e7',
                    padding: '14px 16px',
                    borderRadius: '8px',
                    fontSize: '13px',
                    lineHeight: '1.6',
                    color: '#09090b',
                    whiteSpace: 'pre-wrap'
                  }}
                >
                  {selectedComplaint.description}
                </div>
              </div>

              {/* Existing Resolution Note if any */}
              {selectedComplaint.resolutionNote && (
                <div style={{ marginBottom: '20px', padding: '12px 14px', backgroundColor: '#ecfdf5', borderRadius: '8px', border: '1px solid #a7f3d0' }}>
                  <div style={{ fontSize: '11px', fontWeight: 800, color: '#065f46', textTransform: 'uppercase' }}>
                    Admin Resolution Note:
                  </div>
                  <div style={{ fontSize: '13px', color: '#065f46', marginTop: '2px', fontWeight: 600 }}>
                    {selectedComplaint.resolutionNote}
                  </div>
                </div>
              )}

              {/* Add resolution note field */}
              {selectedComplaint.status !== 'RESOLVED' && (
                <div style={{ marginBottom: '16px' }}>
                  <div style={{ fontSize: '12px', fontWeight: 700, color: '#09090b', marginBottom: '6px' }}>
                    Add Resolution / Action Note:
                  </div>
                  <textarea
                    rows={3}
                    placeholder="Enter resolution details, refund transaction ID, or message sent to user..."
                    value={resolutionInput}
                    onChange={(e) => setResolutionInput(e.target.value)}
                    style={{
                      width: '100%',
                      padding: '10px 12px',
                      borderRadius: '8px',
                      border: '1px solid #d4d4d8',
                      fontSize: '13px',
                      resize: 'vertical',
                      boxSizing: 'border-box'
                    }}
                  />
                </div>
              )}
            </div>

            {/* Modal Actions Footer */}
            <div
              style={{
                padding: '16px 24px',
                borderTop: '1px solid #e4e4e7',
                backgroundColor: '#ffffff',
                display: 'flex',
                alignItems: 'center',
                justifyContent: 'space-between',
                flexWrap: 'wrap',
                gap: '10px'
              }}
            >
              <div>
                {selectedComplaint.status !== 'DISMISSED' && (
                  <button
                    onClick={handleDismiss}
                    style={{
                      background: 'none',
                      border: 'none',
                      color: '#71717a',
                      fontSize: '13px',
                      fontWeight: 700,
                      cursor: 'pointer'
                    }}
                  >
                    Dismiss Ticket
                  </button>
                )}
              </div>

              <div style={{ display: 'flex', alignItems: 'center', gap: '8px' }}>
                {selectedComplaint.status === 'PENDING' && (
                  <button
                    onClick={handleSetInReview}
                    style={{
                      backgroundColor: '#fffbeb',
                      color: '#b45309',
                      border: '1px solid #fde68a',
                      padding: '8px 14px',
                      borderRadius: '8px',
                      fontSize: '12px',
                      fontWeight: 700,
                      cursor: 'pointer'
                    }}
                  >
                    Mark In Review
                  </button>
                )}

                {selectedComplaint.status !== 'RESOLVED' && (
                  <button
                    onClick={handleResolve}
                    style={{
                      backgroundColor: '#10b981',
                      color: '#ffffff',
                      border: 'none',
                      padding: '8px 18px',
                      borderRadius: '8px',
                      fontSize: '13px',
                      fontWeight: 800,
                      cursor: 'pointer',
                      display: 'flex',
                      alignItems: 'center',
                      gap: '6px'
                    }}
                  >
                    <CheckCircle2 size={16} />
                    Resolve Ticket
                  </button>
                )}

                <button
                  onClick={() => setSelectedComplaint(null)}
                  style={{
                    backgroundColor: '#09090b',
                    color: '#ffffff',
                    border: 'none',
                    padding: '8px 16px',
                    borderRadius: '8px',
                    fontSize: '13px',
                    fontWeight: 700,
                    cursor: 'pointer'
                  }}
                >
                  Close
                </button>
              </div>
            </div>
          </div>
        </div>
      )}
    </div>
  );
};
