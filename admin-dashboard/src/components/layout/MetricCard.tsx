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
  isDangerCard?: boolean;
}

export const MetricCard: React.FC<MetricCardProps> = ({
  title,
  value,
  subtitle,
  change,
  isPositive = true,
  icon: IconComponent,
  gradient = 'linear-gradient(135deg, #10b981, #059669)',
  glowColor = 'rgba(16, 185, 129, 0.25)',
  isDangerCard = false
}) => {
  return (
    <div className="glass-card" style={{
      padding: '22px 24px',
      backgroundColor: '#ffffff',
      border: isDangerCard ? '1.5px solid #fecaca' : '1px solid #e2e8f0',
      boxShadow: '0 4px 18px rgba(0, 0, 0, 0.04)'
    }}>
      <div style={{ display: 'flex', alignItems: 'flex-start', justifyContent: 'space-between' }}>
        <div>
          <p style={{ fontSize: '12px', fontWeight: 800, color: '#64748b', textTransform: 'uppercase', letterSpacing: '0.8px' }}>
            {title}
          </p>
          <h3 style={{
            fontSize: '32px',
            fontWeight: 800,
            color: isDangerCard ? '#ef4444' : '#09090b',
            marginTop: '8px',
            letterSpacing: '-0.8px'
          }}>
            {value}
          </h3>
        </div>
        <div style={{
          width: '48px',
          height: '48px',
          borderRadius: '14px',
          background: gradient,
          display: 'flex',
          alignItems: 'center',
          justifyContent: 'center',
          boxShadow: `0 6px 16px ${glowColor}`
        }}>
          <IconComponent size={22} color="#ffffff" />
        </div>
      </div>

      {(subtitle || change) && (
        <div style={{ display: 'flex', alignItems: 'center', gap: '8px', marginTop: '16px', fontSize: '12px' }}>
          {change && (
            <span style={{
              fontWeight: 800,
              color: isPositive ? '#047857' : '#b91c1c',
              background: isPositive ? '#ecfdf5' : '#fef2f2',
              border: `1px solid ${isPositive ? '#a7f3d0' : '#fecaca'}`,
              padding: '2px 8px',
              borderRadius: '6px'
            }}>
              {change}
            </span>
          )}
          {subtitle && (
            <span style={{ color: '#64748b', fontWeight: 500 }}>{subtitle}</span>
          )}
        </div>
      )}
    </div>
  );
};
