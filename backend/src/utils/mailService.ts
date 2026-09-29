import nodemailer, { type Transporter } from 'nodemailer';
import { ENV } from '../config/env.js';

interface SmtpConfig {
  host: string;
  port: number;
  secure: boolean;
  user: string;
  pass: string;
  from: string;
}

let transporter: Transporter | null = null;

function getSmtpConfig(): SmtpConfig {
  return {
    host: process.env.SMTP_HOST || 'smtp.gmail.com',
    port: parseInt(process.env.SMTP_PORT || '587', 10),
    secure: process.env.SMTP_SECURE === 'true',
    user: process.env.SMTP_USER || '',
    pass: process.env.SMTP_PASS || '',
    from: process.env.SMTP_FROM || 'SkillBuilder Support <no-reply@skillbuilder.app>',
  };
}

async function getOrCreateTransporter(): Promise<Transporter> {
  if (transporter) return transporter;

  const config = getSmtpConfig();

  // If user provided real SMTP credentials:
  if (config.user && config.pass) {
    transporter = nodemailer.createTransport({
      host: config.host,
      port: config.port,
      secure: config.secure,
      auth: {
        user: config.user,
        pass: config.pass,
      },
    });
    console.log(`[MailService] Configured real SMTP via ${config.host}:${config.port} (${config.user})`);
    return transporter;
  }

  // Fallback in dev: Create Ethereal test account or local send
  try {
    const testAccount = await nodemailer.createTestAccount();
    transporter = nodemailer.createTransport({
      host: testAccount.smtp.host,
      port: testAccount.smtp.port,
      secure: testAccount.smtp.secure,
      auth: {
        user: testAccount.user,
        pass: testAccount.pass,
      },
    });
    console.log(`[MailService] Ethereal test email account created: ${testAccount.user}`);
    return transporter;
  } catch (err) {
    console.warn(`[MailService] Could not initialize Ethereal test account, using JSON transport:`, err);
    transporter = nodemailer.createTransport({
      jsonTransport: true,
    });
    return transporter;
  }
}

export class MailService {
  /**
   * Dispatches a secure 6-digit OTP code to the user's email address.
   */
  static async sendPasswordResetOtp(email: string, otp: string, role = 'Learner'): Promise<{ success: boolean; previewUrl?: string }> {
    const config = getSmtpConfig();
    const mailer = await getOrCreateTransporter();

    const subject = `Your SkillBuilder Password Reset Code: ${otp}`;
    const html = `
      <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, Helvetica, Arial, sans-serif; max-width: 580px; margin: 0 auto; background: #ffffff; border-radius: 12px; overflow: hidden; border: 1px solid #e2e8f0;">
        <div style="background: linear-gradient(135deg, #10b981 0%, #059669 100%); padding: 32px 24px; text-align: center;">
          <h1 style="color: #ffffff; margin: 0; font-size: 26px; font-weight: 800; letter-spacing: -0.5px;">SkillBuilder</h1>
          <p style="color: #d1fae5; margin: 6px 0 0 0; font-size: 14px;">Learn Skill. Swap Skill. Build Together.</p>
        </div>
        <div style="padding: 32px 28px;">
          <h2 style="color: #1e293b; font-size: 20px; font-weight: 700; margin: 0 0 12px 0;">Password Reset Request</h2>
          <p style="color: #475569; font-size: 15px; line-height: 1.6; margin: 0 0 24px 0;">
            Hello ${role},<br/><br/>
            We received a request to reset your password for your SkillBuilder account (<strong>${email}</strong>). Use the 6-digit verification code below to confirm your identity:
          </p>
          <div style="background: #f8fafc; border: 2px dashed #10b981; border-radius: 12px; padding: 20px; text-align: center; margin: 24px 0;">
            <div style="font-size: 12px; font-weight: 700; text-transform: uppercase; letter-spacing: 1px; color: #64748b; margin-bottom: 8px;">Your 6-Digit OTP Code</div>
            <div style="font-family: monospace, Courier; font-size: 38px; font-weight: 900; letter-spacing: 8px; color: #059669;">${otp}</div>
            <div style="font-size: 12px; color: #94a3b8; margin-top: 8px;">⏱️ Valid for 10 minutes</div>
          </div>
          <p style="color: #64748b; font-size: 13px; line-height: 1.5; margin: 24px 0 0 0;">
            <strong>Important Security Notice:</strong> Never share this code with anyone. SkillBuilder team members will never ask for your verification code.
          </p>
          <p style="color: #94a3b8; font-size: 12px; line-height: 1.5; margin: 16px 0 0 0;">
            If you did not request a password reset, you can safely ignore this email. Your current password remains secure.
          </p>
        </div>
        <div style="background: #f1f5f9; padding: 16px 24px; text-align: center; font-size: 12px; color: #94a3b8;">
          &copy; ${new Date().getFullYear()} SkillBuilder Platform. All rights reserved.
        </div>
      </div>
    `;

    try {
      const info = await mailer.sendMail({
        from: config.from,
        to: email,
        subject,
        html,
        text: `Your SkillBuilder verification code is: ${otp}. It is valid for 10 minutes.`,
      });

      const previewUrl = nodemailer.getTestMessageUrl(info) || undefined;
      console.log(`[MailService] OTP email dispatched to ${email}. MessageId: ${info.messageId}`);
      if (previewUrl) {
        console.log(`[MailService] Preview URL (Ethereal): ${previewUrl}`);
      }
      return { success: true, previewUrl: typeof previewUrl === 'string' ? previewUrl : undefined };
    } catch (error) {
      console.error(`[MailService] Failed to dispatch email to ${email}:`, error);
      return { success: false };
    }
  }
}
