import React from 'react';
import { Search, Bell, ShieldAlert, CheckCircle2 } from 'lucide-react';

interface HeaderProps {
  title: string;
  subtitle?: string;
  searchQuery?: string;
  onSearchChange?: (query: string) => void;
  pendingReportsCount: number;
}

export const AdminHeader: React.FC<HeaderProps> = ({
  title,
  subtitle,
  searchQuery = '',
  onSearchChange,
  pendingReportsCount,
}) => {
  return (
    <header style={{
      height: 'var(--header-height)',
      backgroundColor: 'rgba(17, 24, 39, 0.8)',
      backdropFilter: 'blur(12px)',
      borderBottom: '1px solid var(--border-subtle)',
      position: 'sticky',
      top: 0,
      zIndex: 40,
      display: 'flex',
      alignItems: 'center',
      justifyContent: 'space-between',
      padding: '0 32px'
    }}>
      {/* Title & Subtitle */}
      <div>
        <h1 style={{ fontSize: '20px', fontWeight: 800, color: 'var(--text-primary)' }}>{title}</h1>
        {subtitle && <p style={{ fontSize: '12px', color: 'var(--text-secondary)' }}>{subtitle}</p>}
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

        {/* Live System Status Pill */}
        <div style={{
          display: 'flex',
          alignItems: 'center',
          gap: '8px',
          padding: '6px 12px',
          borderRadius: 'var(--radius-full)',
          background: 'rgba(16, 185, 129, 0.1)',
          border: '1px solid rgba(16, 185, 129, 0.25)',
          fontSize: '12px',
          color: 'var(--success)',
          fontWeight: 600
        }}>
          <span style={{
            width: '8px',
            height: '8px',
            borderRadius: '50%',
            backgroundColor: 'var(--success)',
            boxShadow: '0 0 8px var(--success)'
          }} />
          <span>Server Live & Connected</span>
        </div>

        {/* Notification / Report Bell */}
        <div style={{ position: 'relative' }}>
          <button
            className="btn btn-outline"
            style={{
              padding: '8px',
              borderRadius: '50%',
              width: '40px',
              height: '40px'
            }}
            title={`${pendingReportsCount} pending bad practice alerts`}
          >
            {pendingReportsCount > 0 ? (
              <ShieldAlert size={18} color="var(--warning)" />
            ) : (
              <Bell size={18} />
            )}
          </button>
          {pendingReportsCount > 0 && (
            <span style={{
              position: 'absolute',
              top: '-4px',
              right: '-4px',
              backgroundColor: 'var(--danger)',
              color: '#fff',
              fontSize: '10px',
              fontWeight: 800,
              borderRadius: '50%',
              width: '18px',
              height: '18px',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              border: '2px solid var(--bg-secondary)'
            }}>
              {pendingReportsCount}
            </span>
          )}
        </div>
      </div>
    </header>
  );
};
