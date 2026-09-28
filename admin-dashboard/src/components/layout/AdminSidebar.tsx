import {
  Users,
  LifeBuoy,
  RotateCcw,
  Sparkles,
  ShieldCheck,
  LogOut
} from 'lucide-react';

interface SidebarProps {
  currentTab: string;
  onSelectTab: (tab: string) => void;
  pendingComplaintsCount: number;
  blockedUsersCount: number;
  onResetData: () => void;
  onLogout?: () => void;
}

export const AdminSidebar: React.FC<SidebarProps> = ({
  currentTab,
  onSelectTab,
  pendingComplaintsCount,
  blockedUsersCount,
  onResetData,
  onLogout
}) => {
  const navItems = [
    {
      id: 'users',
      label: 'User Management',
      icon: Users,
      badge: blockedUsersCount > 0 ? `${blockedUsersCount} Blocked` : undefined,
      badgeType: 'danger'
    },
    {
      id: 'support',
      label: 'Help & Support',
      icon: LifeBuoy,
      badge: pendingComplaintsCount > 0 ? `${pendingComplaintsCount} New` : undefined,
      badgeType: 'danger'
    },
  ];

  return (
    <aside
      style={{
        width: 'var(--sidebar-width)',
        height: '100vh',
        position: 'fixed',
        left: 0,
        top: 0,
        backgroundColor: '#ffffff',
        borderRight: '1px solid #e2e8f0',
        display: 'flex',
        flexDirection: 'column',
        zIndex: 50,
        boxShadow: '2px 0 12px rgba(0, 0, 0, 0.03)'
      }}
    >
      {/* Brand Header */}
      <div
        style={{
          padding: '22px 20px',
          borderBottom: '1px solid #e2e8f0',
          display: 'flex',
          alignItems: 'center',
          gap: '12px',
          backgroundColor: '#ffffff'
        }}
      >
        <div
          style={{
            width: '42px',
            height: '42px',
            borderRadius: '12px',
            background: '#09090b',
            display: 'flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 4px 12px rgba(0, 0, 0, 0.15)',
            overflow: 'hidden'
          }}
        >
          <ShieldCheck size={24} color="#10b981" />
        </div>
        <div>
          <div style={{ display: 'flex', alignItems: 'center', gap: '6px' }}>
            <span style={{ fontWeight: 800, fontSize: '18px', letterSpacing: '-0.3px', color: '#09090b' }}>
              SkillBuilder
            </span>
            <span
              style={{
                background: '#ecfdf5',
                color: '#047857',
                border: '1px solid #a7f3d0',
                fontSize: '10px',
                padding: '2px 6px',
                borderRadius: '4px',
                fontWeight: 800
              }}
            >
              ADMIN
            </span>
          </div>
          <p style={{ fontSize: '12px', color: '#64748b', fontWeight: 500, margin: 0 }}>
            Users & Support Portal
          </p>
        </div>
      </div>

      {/* Navigation */}
      <div style={{ padding: '20px 14px', flex: 1, overflowY: 'auto' }}>
        <p
          style={{
            fontSize: '11px',
            fontWeight: 800,
            color: '#64748b',
            letterSpacing: '1px',
            padding: '0 10px 10px'
          }}
        >
          CONSOLE NAVIGATION
        </p>
        <nav style={{ display: 'flex', flexDirection: 'column', gap: '8px' }}>
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
                  padding: '12px 14px',
                  borderRadius: '10px',
                  border: 'none',
                  background: isActive ? '#ecfdf5' : 'transparent',
                  color: isActive ? '#047857' : '#334155',
                  cursor: 'pointer',
                  fontWeight: isActive ? 800 : 600,
                  fontSize: '14px',
                  borderLeft: isActive ? '4px solid #10b981' : '4px solid transparent',
                  boxShadow: isActive ? '0 2px 8px rgba(16, 185, 129, 0.12)' : 'none',
                  transition: 'all 0.15s ease',
                  textAlign: 'left'
                }}
              >
                <div style={{ display: 'flex', alignItems: 'center', gap: '12px' }}>
                  <IconComponent size={19} color={isActive ? '#10b981' : '#64748b'} />
                  <span>{item.label}</span>
                </div>
                {item.badge && (
                  <span
                    className={`badge badge-${item.badgeType}`}
                    style={{
                      fontSize: '11px',
                      padding: '2px 8px',
                      borderRadius: '12px',
                      backgroundColor: '#fef2f2',
                      color: '#ef4444',
                      border: '1px solid #fecaca',
                      fontWeight: 800
                    }}
                  >
                    {item.badge}
                  </span>
                )}
              </button>
            );
          })}
        </nav>

        <div style={{ marginTop: '36px', padding: '0 10px' }}>
          <p
            style={{
              fontSize: '11px',
              fontWeight: 800,
              color: '#64748b',
              letterSpacing: '1px',
              marginBottom: '10px'
            }}
          >
            DATA CONTROLS
          </p>
          <button
            onClick={() => {
              if (window.confirm('Reset all demo moderation data and complaints?')) {
                onResetData();
              }
            }}
            className="btn btn-outline"
            style={{ width: '100%', fontSize: '12px', padding: '9px 12px', justifyContent: 'center' }}
          >
            <RotateCcw size={14} color="#64748b" />
            Reset Demo Data
          </button>
        </div>
      </div>

      {/* Admin User Footer */}
      <div
        style={{
          padding: '16px 20px',
          borderTop: '1px solid #e2e8f0',
          background: '#f8fafc',
          display: 'flex',
          alignItems: 'center',
          gap: '12px'
        }}
      >
        <div style={{ position: 'relative' }}>
          <img
            src="https://images.unsplash.com/photo-1534528741775-53994a69daeb?w=120"
            alt="Admin"
            style={{ width: '38px', height: '38px', borderRadius: '50%', objectFit: 'cover', border: '1.5px solid #e2e8f0' }}
          />
          <div
            style={{
              position: 'absolute',
              bottom: 0,
              right: 0,
              width: '10px',
              height: '10px',
              borderRadius: '50%',
              backgroundColor: '#10b981',
              border: '2px solid #ffffff'
            }}
          />
        </div>
        <div style={{ flex: 1, minWidth: 0 }}>
          <div style={{ display: 'flex', alignItems: 'center', gap: '4px' }}>
            <span style={{ fontSize: '13px', fontWeight: 800, color: '#09090b', overflow: 'hidden', textOverflow: 'ellipsis', whiteSpace: 'nowrap' }}>
              Super Admin
            </span>
            <Sparkles size={12} color="#10b981" />
          </div>
          <p style={{ fontSize: '11px', color: '#64748b', fontWeight: 500, margin: 0 }}>
            admin@skillbuilder.io
          </p>
        </div>

        {onLogout && (
          <button
            onClick={onLogout}
            title="Log Out of Admin Panel"
            style={{
              background: 'none',
              border: '1px solid #e2e8f0',
              borderRadius: '8px',
              padding: '7px',
              cursor: 'pointer',
              color: '#ef4444',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              backgroundColor: '#ffffff',
              transition: 'all 0.15s ease'
            }}
            onMouseEnter={(e) => (e.currentTarget.style.backgroundColor = '#fef2f2')}
            onMouseLeave={(e) => (e.currentTarget.style.backgroundColor = '#ffffff')}
          >
            <LogOut size={16} />
          </button>
        )}
      </div>
    </aside>
  );
};
