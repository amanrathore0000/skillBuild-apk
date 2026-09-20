import React from 'react';
import {
  LayoutDashboard,
  Users,
  Video,
  AlertTriangle,
  FileText,
  RotateCcw,
  Sparkles
} from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  onSelectTab: (tab: string) => void;
  pendingReportsCount: number;
  blockedUsersCount: number;
  onResetData: () => void;
}

export const AdminSidebar: React.FC<SidebarProps> = ({
  currentTab,
  onSelectTab,
  pendingReportsCount,
  blockedUsersCount,
  onResetData
}) => {
  const navItems = [
    {
      id: 'overview',
      label: 'Dashboard Overview',
      icon: LayoutDashboard,
    },
    {
      id: 'users',
      label: 'User Management',
      icon: Users,
      badge: blockedUsersCount > 0 ? `${blockedUsersCount} Blocked` : undefined,
      badgeType: 'danger'
    },
    {
      id: 'uploads',
      label: 'Uploaded Content',
      icon: Video,
    },
    {
      id: 'reports',
      label: 'Bad Practice Reports',
      icon: AlertTriangle,
      badge: pendingReportsCount > 0 ? `${pendingReportsCount} Alert` : undefined,
      badgeType: 'warning'
    },
    {
      id: 'audit',
      label: 'Admin Audit Logs',
      icon: FileText,
    },
  ];

  return (
    <aside style={{
      width: 'var(--sidebar-width)',
      height: '100vh',
      position: 'fixed',
      left: 0,
      top: 0,
      backgroundColor: 'var(--bg-secondary)',
      borderRight: '1px solid var(--border-subtle)',
      display: 'flex',
      flexDirection: 'column',
      zIndex: 50
    }}>
      {/* Brand Header */}
      <div style={{
        padding: '24px 20px',
        borderBottom: '1px solid var(--border-subtle)',
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
      }}>
        <div style={{
          width: '42px',
          height: '42px',
          borderRadius: '12px',
          background: '#111113',
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: '0 4px 14px rgba(239, 68, 68, 0.25)',
          overflow: 'hidden'
        }}>
          <img
            src="/logo.png"
            alt="SkillBuilder"
            style={{ width: '100%', height: '100%', objectFit: 'contain' }}
          />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontWeight: 800, fontSize: '18px', letterSpacing: '-0.3px' }}>SkillBuilder</span>
            <span style={{
              background: 'rgba(99, 102, 241, 0.15)',
              color: 'var(--primary-light)',
              fontSize: '10px',
              padding: '2px 6px',
              borderRadius: '4px',
              fontWeight: 800
            }}>ADMIN</span>
          </div>
          <p style={{ fontSize: '12px', color: 'var(--text-muted)' }}>Governance & Safety Hub</p>
        </div>
      </div>

      {/* Navigation */}
      <div style={{ padding: '20px 14px', flex: 1, overflowY: 'auto' }}>
        <p style={{
          fontSize: '11px',
          fontWeight: 700,
          color: 'var(--text-muted)',
          letterSpacing: '1px',
          padding: '0 10px 10px'
        }}>
          MANAGEMENT CONSOLE
        </p>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '6px' }}>
          {navItems.map((item) => {
            const IconComponent = item.icon;
            const isActive = currentTab === item.id;
            return (
              <button
                key={item.id}
                onClick={() => onSelectTab(item.id)}
                style={{
                  display: 'flex',
                  alignItems: 'center',
                  justifyContent: 'space-between',
                  width: '100%',
                  padding: '11px 14px',
                  borderRadius: '10px',
                  border: 'none',
                  background: isActive ? 'linear-gradient(135deg, rgba(99, 102, 241, 0.18), rgba(99, 102, 241, 0.06))' : 'transparent',
                  color: isActive ? 'var(--primary-light)' : 'var(--text-secondary)',
                  cursor: 'pointer',
                  fontWeight: isActive ? 700 : 500,
                  fontSize: '14px',
                  borderLeft: isActive ? '3px solid var(--primary)' : '3px solid transparent',
                  transition: 'all 0.2s ease',
                  textAlign: 'left'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <IconComponent size={18} color={isActive ? 'var(--primary-light)' : 'currentColor'} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span className={`badge badge-${item.badgeType}`} style={{ fontSize: '10px', padding: '2px 7px' }}>
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        <div style={{ marginTop: '28px', padding: '0 10px' }}>
          <p style={{
            fontSize: '11px',
            fontWeight: 700,
            color: 'var(--text-muted)',
            letterSpacing: '1px',
            marginBottom: '10px'
          }}>
            QUICK CONTROLS
          </p>
          <button
            onClick={() => {
              if (window.confirm('Reset all demo moderation data and test accounts?')) {
                onResetData();
              }
            }}
            className="btn btn-outline"
            style={{ width: '100%', fontSize: '12px', padding: '8px 12px' }}
          >
            <RotateCcw size={14} />
            Reset Demo Data
          </button>
        </div>
      </div>

      {/* Admin User Footer */}
      <div style={{
        padding: '16px 20px',
        borderTop: '1px solid var(--border-subtle)',
        background: 'rgba(0, 0, 0, 0.2)',
        display: 'flex',
        alignItems: 'center',
        gap: '12px'
      }}>
        <div style={{ position: 'relative' }}>
          <img
            src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120"
            alt="Admin"
            style={{ width: '38px', height: '38px', borderRadius: '50%', objectFit: 'cover' }}
          />
          <div style={{
            position: 'absolute',
            bottom: 0,
            right: 0,
            width: '10px',
            height: '10px',
            borderRadius: '50%',
            backgroundColor: 'var(--success)',
            border: '2px solid var(--bg-secondary)'
          }} />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ fontSize: '13px', fontWeight: 700, overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>Super Admin</span>
            <Sparkles size={12} color="var(--warning)" />
          </div>
          <p style={{ fontSize: '11px', color: 'var(--text-muted)' }}>admin@skillbuilder.io</p>
        </div>
      </div>
    </aside>
  );
};
