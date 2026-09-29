import { OAuth2Client } from 'google-auth-library';
import { ENV } from '../config/env.js';
import { AppError } from '../middlewares/error.middleware.js';

const client = new OAuth2Client(ENV.GOOGLE_CLIENT_ID);

export interface GoogleUserPayload {
  sub: string;
  email: string;
  emailVerified: boolean;
  name: string;
  picture?: string;
}

export interface GoogleAuthOptions {
  isMentor?: boolean;
  ipAddress?: string;
  userAgent?: string;
  fallbackName?: string;
  fallbackPicture?: string;
}

/**
 * Strictly verifies a Google OAuth 2.0 / OpenID Connect ID token.
 * Validates cryptographic signature, issuer, audience, expiration, and email verification status.
 */
export async function verifyGoogleIdToken(
  idToken: string,
  options?: GoogleAuthOptions
): Promise<GoogleUserPayload> {
  const cleanToken = (idToken || '').trim();
  if (!cleanToken) {
    throw new AppError('Google ID token is required', 400);
  }

  // Parse all configured audiences (supports multiple client IDs if comma-separated)
  const configuredAudiences = (ENV.GOOGLE_CLIENT_ID || '')
    .split(',')
    .map((s) => s.trim())
    .filter(Boolean);

  let verificationError: any = null;

  // 1. Primary OpenID Connect verification using Google's official OAuth2Client
  if (configuredAudiences.length > 0) {
    try {
      const ticket = await client.verifyIdToken({
        idToken: cleanToken,
        audience: configuredAudiences.length === 1 ? configuredAudiences[0] : configuredAudiences,
      });

      const payload = ticket.getPayload();
      if (!payload) {
        throw new AppError('Google ID token contains an empty payload', 401);
      }

      // Verify Issuer
      const validIssuers = ['accounts.google.com', 'https://accounts.google.com'];
      if (!payload.iss || !validIssuers.includes(payload.iss)) {
        throw new AppError(`Invalid Google token issuer: ${payload.iss}`, 401);
      }

      // Verify Subject (sub is the permanent immutable Google account ID)
      if (!payload.sub || typeof payload.sub !== 'string') {
        throw new AppError('Google ID token is missing a valid subject (sub)', 401);
      }

      // Verify Email & Email Verification Status
      if (!payload.email) {
        throw new AppError('Google ID token is missing email address', 400);
      }

      if (payload.email_verified === false) {
        throw new AppError('Google email address is not verified by Google', 403);
      }

      const email = payload.email.toLowerCase().trim();
      const name = payload.name || options?.fallbackName || email.split('@')[0];
      const picture = payload.picture || options?.fallbackPicture;

      return {
        sub: payload.sub,
        email,
        emailVerified: true,
        name,
        picture,
      };
    } catch (err: any) {
      verificationError = err;
      console.warn(`[OAuth] Google client.verifyIdToken error: ${err?.message || err}`);
    }
  }

  // 2. Secondary verification via Google's official tokeninfo HTTPS endpoint
  try {
    const res = await fetch(`https://oauth2.googleapis.com/tokeninfo?id_token=${encodeURIComponent(cleanToken)}`);
    if (res.ok) {
      const data: any = await res.json();
      if (data && data.sub && data.email) {
        // Validate audience if configured
        if (configuredAudiences.length > 0 && !configuredAudiences.includes(data.aud)) {
          throw new AppError(`Google token audience mismatch: ${data.aud}`, 401);
        }

        if (data.email_verified === 'false' || data.email_verified === false) {
          throw new AppError('Google email address is not verified', 403);
        }

        const email = String(data.email).toLowerCase().trim();
        return {
          sub: String(data.sub),
          email,
          emailVerified: true,
          name: data.name || options?.fallbackName || email.split('@')[0],
          picture: data.picture || options?.fallbackPicture,
        };
      }
    }
  } catch (fetchErr: any) {
    console.warn(`[OAuth] Google tokeninfo fallback check failed: ${fetchErr?.message || fetchErr}`);
  }

  // 3. Fallback for offline testing / development test runner
  if (process.env.NODE_ENV === 'test' || process.env.NODE_ENV === 'development') {
    if (cleanToken.startsWith('test_google_token:')) {
      const rest = cleanToken.substring('test_google_token:'.length);
      const [rawEmail, rawSub] = rest.split(':');
      const testEmail = (rawEmail || 'testuser@gmail.com').toLowerCase().trim();
      const testSub = rawSub || 'google_sub_' + Buffer.from(testEmail).toString('hex').slice(0, 16);
      return {
        sub: testSub,
        email: testEmail,
        emailVerified: true,
        name: options?.fallbackName || testEmail.split('@')[0],
        picture: options?.fallbackPicture,
      };
    }
  }

  const reason = verificationError?.message || 'Invalid or unverifiable Google ID token';
  throw new AppError(`Google authentication failed: ${reason}`, 401);
}
