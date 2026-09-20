import React from 'react';
import { LucideIcon } from 'lucide-react';

interface MetricCardProps {
  title: string;
  value: string | number;
  subtitle?: string;
  change?: string;
  isPositive?: boolean;
  icon: LucideIcon;
  gradient?: string;
  glowColor?: string;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  change,
  isPositive = true,
  icon: IconComponent,
  gradient = 'linear-gradient(135deg, #6366f1, #4f46e5)',
  glowColor = 'rgba(99, 102, 241, 0.25)'
}) => {
  return (
    <div className="glass-card" style={{ padding: '22px' }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <p style={{ fontSize: '12px', fontWeight: 700, color: 'var(--text-secondary)', textTransform: 'uppercase', letterSpacing: '0.8px' }}>
            {title}
          </p>
          <h3 style={{ fontSize: '28px', fontWeight: 800, color: 'var(--text-primary)', marginTop: '8px', letterSpacing: '-0.5px' }}>
            {value}
          </h3>
        </div>
        <div style={{
          width: '46px',
          height: '46px',
          borderRadius: '12px',
          background: gradient,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: `0 8px 16px ${glowColor}`
        }}>
          <IconComponent size={22} color="#ffffff" />
        </div>
      </div>

      {(subtitle || change) && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '14px', fontSize: '12px' }}>
          {change && (
            <span style={{
              fontWeight: 700,
              color: isPositive ? 'var(--success)' : 'var(--danger)',
              background: isPositive ? 'rgba(16, 185, 129, 0.12)' : 'rgba(239, 68, 68, 0.12)',
              padding: '2px 8px',
              borderRadius: '6px'
            }}>
              {change}
            </span>
          )}
          {subtitle && (
            <span style={{ color: 'var(--text-muted)' }}>{subtitle}</span>
          )}
        </div>
      )}
    </div>
  );
};
