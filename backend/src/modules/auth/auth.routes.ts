import { Router, Request, Response, NextFunction } from 'express';
import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import { AuthController } from './auth.controller.js';
import { validate } from '../../middlewares/validate.middleware.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';
import { GoogleLoginSchema, SignupSchema, LoginSchema, RefreshTokenSchema } from './auth.validation.js';
import { ENV } from '../../config/env.js';

export const authRouter = Router();

// Rate limiting mechanism for login attempts
const loginAttempts = new Map<string, { count: number; resetTime: number }>();
const MAX_ATTEMPTS = 5;
const WINDOW_MS = 15 * 60 * 1000;

const authRateLimiter = (req: Request, res: Response, next: NextFunction) => {
  const ip = req.ip || req.socket.remoteAddress || 'unknown';
  const now = Date.now();
  const record = loginAttempts.get(ip);

  if (record && record.resetTime > now) {
    if (record.count >= MAX_ATTEMPTS) {
      return res.status(429).json({
        success: false,
        message: 'Too many login attempts. Please try again after 15 minutes.'
      });
    }
    record.count++;
  } else {
    loginAttempts.set(ip, { count: 1, resetTime: now + WINDOW_MS });
  }
  next();
};

authRouter.post('/google', validate(GoogleLoginSchema), AuthController.googleAuth);
authRouter.post('/signup', validate(SignupSchema), AuthController.signup);
authRouter.post('/login', validate(LoginSchema), AuthController.login);
authRouter.post('/refresh', validate(RefreshTokenSchema), AuthController.refresh);
authRouter.get('/me', requireAuth, AuthController.me);

authRouter.post('/admin/login', authRateLimiter, (req: Request, res: Response) => {
  const { loginId, password } = req.body;
  const cleanId = (loginId || '').trim();
  const cleanPass = (password || '').trim();

  const configuredAdminId = (ENV.ADMIN_LOGIN_ID || 'admin').trim();
  const configuredHashHex = (
    ENV.ADMIN_PASSWORD_HASH ||
    crypto.createHash('sha256').update('SkillBuilderAdmin#2026').digest('hex')
  ).trim();

  // Constant-time comparison for ID
  const idHash = crypto.createHash('sha256').update(cleanId.toLowerCase()).digest();
  const targetIdHash = crypto.createHash('sha256').update(configuredAdminId.toLowerCase()).digest();
  const isIdValid = crypto.timingSafeEqual(idHash, targetIdHash);

  // Constant-time comparison for password hash
  const inputPassHash = crypto.createHash('sha256').update(cleanPass).digest();
  let expectedPassHash: Buffer;
  try {
    expectedPassHash = Buffer.from(configuredHashHex, 'hex');
    if (expectedPassHash.length !== 32) {
      expectedPassHash = crypto.createHash('sha256').update(configuredHashHex).digest();
    }
  } catch {
    expectedPassHash = crypto.createHash('sha256').update(configuredHashHex).digest();
  }
  const isPassValid = crypto.timingSafeEqual(inputPassHash, expectedPassHash);

  if (!isIdValid || !isPassValid) {
    res.status(401).json({
      success: false,
      message: 'Invalid Admin Login ID or Password.'
    });
    return;
  }

  const token = jwt.sign(
    {
      userId: 'adm_root_001',
      email: 'admin@skillbuilder.io',
      role: 'SUPER_ADMIN',
      isAdmin: true
    },
    ENV.JWT_SECRET,
    { expiresIn: '7d' }
  );

  res.status(200).json({
    success: true,
    message: 'Admin authenticated successfully.',
    data: {
      token,
      role: 'SUPER_ADMIN',
      email: 'admin@skillbuilder.io',
      name: 'Super Administrator'
    }
  });
});

authRouter.get('/admin/verify', (req: Request, res: Response) => {
  const authHeader = req.headers.authorization;
  if (!authHeader || !authHeader.startsWith('Bearer ')) {
    return res.status(401).json({ success: false, valid: false, message: 'Missing token' });
  }
  const token = authHeader.split(' ')[1];
  try {
    const decoded = jwt.verify(token, ENV.JWT_SECRET);
    return res.status(200).json({ success: true, valid: true, data: decoded });
  } catch {
    return res.status(401).json({ success: false, valid: false, message: 'Invalid or expired token' });
  }
});

