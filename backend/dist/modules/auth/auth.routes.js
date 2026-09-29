import { Router } from 'express';
import crypto from 'crypto';
import jwt from 'jsonwebtoken';
import bcrypt from 'bcryptjs';
import { AuthController } from './auth.controller.js';
import { validate } from '../../middlewares/validate.middleware.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';
import { GoogleLoginSchema, SignupSchema, LoginSchema, RefreshTokenSchema } from './auth.validation.js';
import { ENV } from '../../config/env.js';
import { MailService } from '../../utils/mailService.js';
import { prisma } from '../../config/database.js';
export const authRouter = Router();
// Rate limiting mechanism for login attempts
const loginAttempts = new Map();
const MAX_ATTEMPTS = 5;
const WINDOW_MS = 15 * 60 * 1000;
const authRateLimiter = (req, res, next) => {
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
    }
    else {
        loginAttempts.set(ip, { count: 1, resetTime: now + WINDOW_MS });
    }
    next();
};
authRouter.post('/google', validate(GoogleLoginSchema), AuthController.googleAuth);
authRouter.post('/signup', validate(SignupSchema), AuthController.signup);
authRouter.post('/login', validate(LoginSchema), AuthController.login);
authRouter.post('/logout', requireAuth, AuthController.logout);
authRouter.post('/refresh', validate(RefreshTokenSchema), AuthController.refresh);
authRouter.get('/me', requireAuth, AuthController.me);
const activeOtpSessions = new Map();
// 1. Forgot Password: Generate 6-digit OTP and send to user's real email
authRouter.post('/forgot-password', async (req, res) => {
    try {
        const { email } = req.body;
        const cleanEmail = (email || '').trim().toLowerCase();
        if (!cleanEmail || !cleanEmail.includes('@')) {
            return res.status(400).json({
                success: false,
                message: 'Please provide a valid registered email address.',
            });
        }
        const user = await prisma.user.findUnique({ where: { email: cleanEmail } });
        const role = user?.isMentor ? 'Mentor' : 'Learner';
        // Generate cryptographic 6-digit OTP
        const otp = crypto.randomInt(100000, 1000000).toString();
        const expiresAt = Date.now() + 10 * 60 * 1000; // 10 minutes
        activeOtpSessions.set(cleanEmail, {
            email: cleanEmail,
            otp,
            expiresAt,
            attempts: 0,
            isVerified: false,
        });
        // Dispatch real email via MailService (never returns OTP in HTTP response)
        const mailResult = await MailService.sendPasswordResetOtp(cleanEmail, otp, role);
        console.log(`[Auth] OTP generated for ${cleanEmail}. Email dispatched: ${mailResult.success}`);
        return res.status(200).json({
            success: true,
            message: `A secure 6-digit OTP code has been dispatched to ${cleanEmail}. Please check your inbox and spam folder.`,
            data: {
                email: cleanEmail,
                expiresInSeconds: 600,
                emailSent: mailResult.success,
                previewUrl: mailResult.previewUrl,
            },
        });
    }
    catch (error) {
        console.error('[Auth] Error in /forgot-password:', error);
        return res.status(500).json({
            success: false,
            message: 'Failed to process password reset request. Please try again.',
        });
    }
});
// 2. Verify 6-digit OTP code entered by user
authRouter.post('/verify-otp', async (req, res) => {
    try {
        const { email, otp } = req.body;
        const cleanEmail = (email || '').trim().toLowerCase();
        const cleanOtp = (otp || '').trim();
        const record = activeOtpSessions.get(cleanEmail);
        if (!record) {
            return res.status(400).json({
                success: false,
                message: 'No active OTP verification session found. Please request a new code.',
            });
        }
        if (Date.now() > record.expiresAt) {
            activeOtpSessions.delete(cleanEmail);
            return res.status(400).json({
                success: false,
                message: 'This OTP verification code has expired. Please request a new code.',
            });
        }
        record.attempts++;
        if (record.attempts > 5) {
            activeOtpSessions.delete(cleanEmail);
            return res.status(429).json({
                success: false,
                message: 'Too many incorrect attempts. Please request a new OTP code.',
            });
        }
        if (record.otp !== cleanOtp) {
            return res.status(400).json({
                success: false,
                message: 'Invalid verification code. Please check your email and try again.',
            });
        }
        record.isVerified = true;
        activeOtpSessions.set(cleanEmail, record);
        return res.status(200).json({
            success: true,
            message: 'OTP verified successfully.',
            data: {
                email: cleanEmail,
                isVerified: true,
            },
        });
    }
    catch (error) {
        return res.status(500).json({
            success: false,
            message: 'Failed to verify OTP. Please try again.',
        });
    }
});
// 3. Reset Password after successful OTP verification
authRouter.post('/reset-password', async (req, res) => {
    try {
        const { email, otp, newPassword } = req.body;
        const cleanEmail = (email || '').trim().toLowerCase();
        const cleanOtp = (otp || '').trim();
        const cleanPass = (newPassword || '').trim();
        if (!cleanPass || cleanPass.length < 6) {
            return res.status(400).json({
                success: false,
                message: 'Password must be at least 6 characters long.',
            });
        }
        const record = activeOtpSessions.get(cleanEmail);
        if (!record || !record.isVerified || record.otp !== cleanOtp) {
            return res.status(403).json({
                success: false,
                message: 'OTP verification is required before updating password.',
            });
        }
        // Hash password and update in database
        const passwordHash = await bcrypt.hash(cleanPass, 10);
        const existing = await prisma.user.findUnique({ where: { email: cleanEmail } });
        if (existing) {
            await prisma.user.update({
                where: { email: cleanEmail },
                data: { passwordHash },
            });
        }
        activeOtpSessions.delete(cleanEmail);
        return res.status(200).json({
            success: true,
            message: 'Password has been updated successfully. You can now log in.',
        });
    }
    catch (error) {
        return res.status(500).json({
            success: false,
            message: 'Failed to reset password. Please try again.',
        });
    }
});
authRouter.post('/admin/login', authRateLimiter, (req, res) => {
    const { loginId, password } = req.body;
    const cleanId = (loginId || '').trim();
    const cleanPass = (password || '').trim();
    const configuredAdminId = (ENV.ADMIN_LOGIN_ID || 'admin').trim();
    const configuredHashHex = (ENV.ADMIN_PASSWORD_HASH ||
        crypto.createHash('sha256').update('SkillBuilderAdmin#2026').digest('hex')).trim();
    // Constant-time comparison for ID
    const idHash = crypto.createHash('sha256').update(cleanId.toLowerCase()).digest();
    const targetIdHash = crypto.createHash('sha256').update(configuredAdminId.toLowerCase()).digest();
    const isIdValid = crypto.timingSafeEqual(idHash, targetIdHash);
    // Constant-time comparison for password hash
    const inputPassHash = crypto.createHash('sha256').update(cleanPass).digest();
    let expectedPassHash;
    try {
        expectedPassHash = Buffer.from(configuredHashHex, 'hex');
        if (expectedPassHash.length !== 32) {
            expectedPassHash = crypto.createHash('sha256').update(configuredHashHex).digest();
        }
    }
    catch {
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
    const token = jwt.sign({
        userId: 'adm_root_001',
        email: 'admin@skillbuilder.io',
        role: 'SUPER_ADMIN',
        isAdmin: true
    }, ENV.JWT_SECRET, { expiresIn: '7d' });
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
authRouter.get('/admin/verify', (req, res) => {
    const authHeader = req.headers.authorization;
    if (!authHeader || !authHeader.startsWith('Bearer ')) {
        return res.status(401).json({ success: false, valid: false, message: 'Missing token' });
    }
    const token = authHeader.split(' ')[1];
    try {
        const decoded = jwt.verify(token, ENV.JWT_SECRET);
        return res.status(200).json({ success: true, valid: true, data: decoded });
    }
    catch {
        return res.status(401).json({ success: false, valid: false, message: 'Invalid or expired token' });
    }
});
//# sourceMappingURL=auth.routes.js.map