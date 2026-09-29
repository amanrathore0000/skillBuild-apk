import { betterAuth } from 'better-auth';
import { prismaAdapter } from 'better-auth/adapters/prisma';
import { bearer } from 'better-auth/plugins/bearer';
import { twoFactor } from 'better-auth/plugins';
import { passkey } from '@better-auth/passkey';
import { prisma } from './config/database.js';

export const auth = betterAuth({
  appName: 'SkillBuilder',
  database: prismaAdapter(prisma, {
    provider: 'postgresql',
  }),
  emailAndPassword: {
    enabled: true,
    async sendResetPassword(data, request) {
      console.log(`[PASSWORD RESET] Send reset password email to: ${data.user.email}, URL: ${data.url}`);
    },
  },
  socialProviders: {
    google: {
      clientId: process.env.GOOGLE_CLIENT_ID || '',
      clientSecret: process.env.GOOGLE_CLIENT_SECRET || '',
    },
  },
  account: {
    accountLinking: {
      enabled: true,
      trustedProviders: ['google'],
    },
  },
  user: {
    modelName: 'user',
    additionalFields: {
      phone: { type: 'string', required: false, defaultValue: '' },
      dob: { type: 'string', required: false, defaultValue: '' },
      location: { type: 'string', required: false, defaultValue: '' },
      bio: { type: 'string', required: false },
      isMentor: { type: 'boolean', required: false, defaultValue: false },
      isVerified: { type: 'boolean', required: false, defaultValue: true },
    },
  },
  plugins: [
    bearer(),
    twoFactor({
      issuer: 'SkillBuilder',
    }),
    passkey({
      rpID: process.env.RP_ID || 'localhost',
      rpName: 'SkillBuilder',
      origin: process.env.BETTER_AUTH_URL || 'http://localhost:5000',
    }),
  ],
  trustedOrigins: [
    'http://localhost:5000',
    'http://localhost:3000',
    'http://localhost:5173',
    'http://192.168.29.198:5000',
    'http://10.0.2.2:5000',
    '*',
  ],
});

export type Session = typeof auth.$Infer.Session;
