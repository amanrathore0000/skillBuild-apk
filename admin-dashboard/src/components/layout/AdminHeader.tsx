import React from 'react';
import { Search, Bell, ShieldAlert, LogOut } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
  pendingReportsCount: number;
  onLogout?: () => void;
}

export const AdminHeader: React.FC<HeaderProps> = ({
  title,
  subtitle,
  searchQuery = '',
  onSearchChange,
  pendingReportsCount,
  onLogout,
}) => {
  return (
    <header style={{
      height: 'var(--header-height)',
      backgroundColor: '#ffffff',
      borderBottom: '1px solid #e2e8f0',
      position: 'sticky',
      top: 0,
      zIndex: 40,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 32px',
      boxShadow: '0 1px 4px rgba(0, 0, 0, 0.03)'
    }}>
      {/* Title & Subtitle */}
      <div>
        <h1 style={{ fontSize: '20px', fontWeight: 800, color: '#09090b', letterSpacing: '-0.3px' }}>{title}</h1>
        {subtitle && <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500 }}>{subtitle}</p>}
      </div>

      {/* Center/Right Controls */}
      <div style={{ display: 'flex', alignItems: 'center', gap: '16px' }}>
        {onSearchChange && (
          <div className="search-input-wrapper" style={{ width: '320px' }}>
            <Search size={16} />
            <input
              type="text"
              className="search-input"
              placeholder="Search user, mentor, upload or topic..."
              value={searchQuery}
              onChange={(e) => onSearchChange(e.target.value)}
            />
          </div>
        )}

        {/* Live System Status Pill (Green) */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 14px',
          borderRadius: 'var(--radius-full)',
          background: '#ecfdf5',
          border: '1px solid #a7f3d0',
          fontSize: '12px',
          color: '#047857',
          fontWeight: 700
        }}>
          <span style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: '#10b981',
            boxShadow: '0 0 8px rgba(16, 185, 129, 0.6)'
          }} />
          <span>Server Live & Connected</span>
        </div>

        {/* Notification / Report Bell (Red on Alert) */}
        <div style={{ position: 'relative' }}>
          <button
            className="btn btn-outline"
            style={{
              padding: '8px',
              borderRadius: '50%',
              width: '40px',
              height: '40px',
              borderColor: pendingReportsCount > 0 ? '#fecaca' : '#e2e8f0',
              backgroundColor: pendingReportsCount > 0 ? '#fef2f2' : '#ffffff'
            }}
            title={`${pendingReportsCount} pending bad practice alerts`}
          >
            {pendingReportsCount > 0 ? (
              <ShieldAlert size={18} color="#ef4444" />
            ) : (
              <Bell size={18} color="#09090b" />
            )}
          </button>
          {pendingReportsCount > 0 && (
            <span style={{
              position: 'absolute',
              top: '-4px',
              right: '-4px',
              backgroundColor: '#ef4444',
              color: '#ffffff',
              fontSize: '10px',
              fontWeight: 800,
              borderRadius: '50%',
              width: '18px',
              height: '18px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '2px solid #ffffff'
            }}>
              {pendingReportsCount}
            </span>
          )}
        </div>

        {/* Header Log Out Button */}
        {onLogout && (
          <button
            onClick={onLogout}
            className="btn btn-outline"
            style={{
              padding: '8px 14px',
              fontSize: '12px',
              fontWeight: 700,
              color: '#ef4444',
              borderColor: '#fecaca',
              backgroundColor: '#fef2f2',
              display: 'flex',
              alignItems: 'center',
              gap: '6px'
            }}
            title="Log Out of Admin Panel"
          >
            <LogOut size={14} />
            <span>Log Out</span>
          </button>
        )}
      </div>
    </header>
  );
};
