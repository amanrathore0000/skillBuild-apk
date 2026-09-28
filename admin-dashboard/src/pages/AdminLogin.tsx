import React, { useState } from 'react';
import {
  ShieldCheck,
  Lock,
  User,
  Eye,
  EyeOff,
  AlertCircle,
  ArrowRight
} from 'lucide-react';
import { AdminAuthService, AdminSession } from '../services/api';

interface AdminLoginProps {
  onLoginSuccess: (session: AdminSession) => void;
}

export const AdminLogin: React.FC<AdminLoginProps> = ({ onLoginSuccess }) => {
  const [loginId, setLoginId] = useState('');
  const [password, setPassword] = useState('');
  const [showPassword, setShowPassword] = useState(false);
  const [rememberMe, setRememberMe] = useState(true);
  const [isLoading, setIsLoading] = useState(false);
  const [errorMessage, setErrorMessage] = useState<string | null>(null);

  const handleSubmit = (e: React.FormEvent) => {
    e.preventDefault();
    setErrorMessage(null);

    if (!loginId.trim()) {
      setErrorMessage('Please enter your Admin Login ID.');
      return;
    }
    if (!password) {
      setErrorMessage('Please enter your administrator password.');
      return;
    }

    setIsLoading(true);

    AdminAuthService.login(loginId, password, rememberMe)
      .then((res) => {
        setIsLoading(false);
        if (res.success && res.session) {
          onLoginSuccess(res.session);
        } else {
          setErrorMessage(res.error || 'Access Denied: Invalid credentials provided.');
        }
      })
      .catch((err) => {
        setIsLoading(false);
        setErrorMessage(err?.message || 'Access Denied: Invalid credentials provided.');
      });
  };

  return (
    <div
      style={{
        minHeight: '100vh',
        width: '100%',
        backgroundColor: '#ffffff',
        display: 'flex',
        flexDirection: 'column',
        justifyContent: 'center',
        alignItems: 'center',
        padding: '24px',
        boxSizing: 'border-box',
        backgroundImage: 'radial-gradient(#e4e4e7 1px, transparent 1px)',
        backgroundSize: '24px 24px'
      }}
    >
      {/* Brand Header */}
      <div style={{ textAlign: 'center', marginBottom: '28px' }}>
        <div
          style={{
            width: '56px',
            height: '56px',
            borderRadius: '16px',
            backgroundColor: '#09090b',
            display: 'inline-flex',
            alignItems: 'center',
            justifyContent: 'center',
            boxShadow: '0 8px 24px rgba(0, 0, 0, 0.12)',
            marginBottom: '14px'
          }}
        >
          <ShieldCheck size={32} color="#10b981" />
        </div>
        <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'center', gap: '8px' }}>
          <h1
            style={{
              fontSize: '26px',
              fontWeight: 900,
              color: '#09090b',
              letterSpacing: '-0.5px',
              margin: 0
            }}
          >
            SkillBuilder
          </h1>
          <span
            style={{
              fontSize: '11px',
              fontWeight: 800,
              padding: '2px 8px',
              borderRadius: '6px',
              backgroundColor: '#ecfdf5',
              color: '#065f46',
              border: '1px solid #a7f3d0'
            }}
          >
            ADMIN PORTAL
          </span>
        </div>
      </div>

      {/* Main Login Card */}
      <div
        style={{
          width: '100%',
          maxWidth: '440px',
          backgroundColor: '#ffffff',
          borderRadius: '20px',
          border: '1px solid #e4e4e7',
          boxShadow: '0 20px 40px -15px rgba(0, 0, 0, 0.08)',
          padding: '36px 32px',
          boxSizing: 'border-box'
        }}
      >
        <div style={{ marginBottom: '24px' }}>
          <h2 style={{ fontSize: '20px', fontWeight: 800, color: '#09090b', margin: 0 }}>
            Administrator Sign In
          </h2>
          <p style={{ fontSize: '13px', color: '#71717a', margin: '4px 0 0' }}>
            Enter your root administrator credentials to unlock the panel.
          </p>
        </div>

        {/* Error Alert (Crimson Red) */}
        {errorMessage && (
          <div
            style={{
              padding: '12px 14px',
              borderRadius: '10px',
              backgroundColor: '#fef2f2',
              border: '1px solid #fecaca',
              display: 'flex',
              alignItems: 'center',
              gap: '10px',
              marginBottom: '20px',
              animation: 'fadeIn 0.2s ease-out'
            }}
          >
            <AlertCircle size={18} color="#ef4444" style={{ flexShrink: 0 }} />
            <div style={{ fontSize: '13px', color: '#991b1b', fontWeight: 600, lineHeight: 1.4 }}>
              {errorMessage}
            </div>
          </div>
        )}

        <form onSubmit={handleSubmit}>
          {/* Login ID Input */}
          <div style={{ marginBottom: '18px' }}>
            <label
              style={{
                display: 'block',
                fontSize: '12px',
                fontWeight: 700,
                color: '#09090b',
                marginBottom: '6px',
                textTransform: 'uppercase',
                letterSpacing: '0.04em'
              }}
            >
              Admin Login ID / Email
            </label>
            <div style={{ position: 'relative' }}>
              <div
                style={{
                  position: 'absolute',
                  left: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  color: '#71717a',
                  display: 'flex'
                }}
              >
                <User size={16} />
              </div>
              <input
                type="text"
                value={loginId}
                onChange={(e) => setLoginId(e.target.value)}
                placeholder="e.g. admin or admin@skillbuilder.io"
                autoFocus
                style={{
                  width: '100%',
                  padding: '12px 14px 12px 40px',
                  borderRadius: '10px',
                  border: '1.5px solid #e4e4e7',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: '#09090b',
                  outline: 'none',
                  boxSizing: 'border-box',
                  transition: 'border-color 0.15s ease'
                }}
                onFocus={(e) => (e.target.style.borderColor = '#09090b')}
                onBlur={(e) => (e.target.style.borderColor = '#e4e4e7')}
              />
            </div>
          </div>

          {/* Password Input with Eye Toggle */}
          <div style={{ marginBottom: '18px' }}>
            <div style={{ display: 'flex', justifyContent: 'space-between', alignItems: 'center', marginBottom: '6px' }}>
              <label
                style={{
                  fontSize: '12px',
                  fontWeight: 700,
                  color: '#09090b',
                  textTransform: 'uppercase',
                  letterSpacing: '0.04em'
                }}
              >
                Password
              </label>
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  color: '#71717a',
                  fontSize: '11px',
                  fontWeight: 700,
                  display: 'flex',
                  alignItems: 'center',
                  gap: '4px',
                  padding: 0
                }}
              >
                {showPassword ? <EyeOff size={13} /> : <Eye size={13} />}
                {showPassword ? 'Hide' : 'Reveal'}
              </button>
            </div>
            <div style={{ position: 'relative' }}>
              <div
                style={{
                  position: 'absolute',
                  left: '14px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  color: '#71717a',
                  display: 'flex'
                }}
              >
                <Lock size={16} />
              </div>
              <input
                type={showPassword ? 'text' : 'password'}
                value={password}
                onChange={(e) => setPassword(e.target.value)}
                placeholder="••••••••••••"
                style={{
                  width: '100%',
                  padding: '12px 42px 12px 40px',
                  borderRadius: '10px',
                  border: '1.5px solid #e4e4e7',
                  fontSize: '14px',
                  fontWeight: 600,
                  color: '#09090b',
                  outline: 'none',
                  boxSizing: 'border-box',
                  letterSpacing: showPassword ? 'normal' : '0.15em',
                  transition: 'border-color 0.15s ease'
                }}
                onFocus={(e) => (e.target.style.borderColor = '#09090b')}
                onBlur={(e) => (e.target.style.borderColor = '#e4e4e7')}
              />
              <button
                type="button"
                onClick={() => setShowPassword(!showPassword)}
                style={{
                  position: 'absolute',
                  right: '12px',
                  top: '50%',
                  transform: 'translateY(-50%)',
                  background: 'none',
                  border: 'none',
                  cursor: 'pointer',
                  color: '#71717a',
                  padding: '4px'
                }}
              >
                {showPassword ? <EyeOff size={16} /> : <Eye size={16} />}
              </button>
            </div>
          </div>

          {/* Remember me option */}
          <div style={{ display: 'flex', alignItems: 'center', justifyContent: 'space-between', marginBottom: '24px' }}>
            <label style={{ display: 'flex', alignItems: 'center', gap: '8px', cursor: 'pointer' }}>
              <input
                type="checkbox"
                checked={rememberMe}
                onChange={(e) => setRememberMe(e.target.checked)}
                style={{ width: '16px', height: '16px', accentColor: '#09090b', cursor: 'pointer' }}
              />
              <span style={{ fontSize: '13px', color: '#71717a', fontWeight: 600 }}>
                Remember session on this device
              </span>
            </label>
          </div>

          {/* Submit Button */}
          <button
            type="submit"
            disabled={isLoading}
            style={{
              width: '100%',
              padding: '13px 20px',
              borderRadius: '10px',
              backgroundColor: '#09090b',
              color: '#ffffff',
              border: 'none',
              fontSize: '14px',
              fontWeight: 800,
              cursor: isLoading ? 'not-allowed' : 'pointer',
              display: 'flex',
              alignItems: 'center',
              justifyContent: 'center',
              gap: '8px',
              transition: 'background-color 0.15s ease',
              boxShadow: '0 4px 14px rgba(0, 0, 0, 0.12)'
            }}
          >
            {isLoading ? (
              <span>Authenticating...</span>
            ) : (
              <>
                <span>Sign In to Admin Panel</span>
                <ArrowRight size={16} />
              </>
            )}
          </button>
        </form>

      </div>
    </div>
  );
};
