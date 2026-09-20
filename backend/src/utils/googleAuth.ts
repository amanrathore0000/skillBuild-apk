import { OAuth2Client } from 'google-auth-library';
import { ENV } from '../config/env.js';

const client = new OAuth2Client(ENV.GOOGLE_CLIENT_ID);

export interface GoogleUserPayload {
  googleId: string;
  email: string;
  name: string;
  picture?: string;
}

export async function verifyGoogleIdToken(idToken: string): Promise<GoogleUserPayload> {
  // If GOOGLE_CLIENT_ID is not configured in dev, allow decode for development / testing
  if (!ENV.GOOGLE_CLIENT_ID || ENV.GOOGLE_CLIENT_ID.startsWith('your-google')) {
    try {
      const ticket = await client.verifyIdToken({
        idToken,
      });
      const payload = ticket.getPayload();
      if (!payload || !payload.email) {
        throw new Error('Invalid Google token payload');
      }
      return {
        googleId: payload.sub,
        email: payload.email,
        name: payload.name || payload.email.split('@')[0],
        picture: payload.picture,
      };
    } catch {
      // In local dev without live google client ID, allow basic payload extraction
      const parts = idToken.split('.');
      if (parts.length === 3) {
        const decoded = JSON.parse(Buffer.from(parts[1], 'base64').toString('utf8'));
        return {
          googleId: decoded.sub || 'google-sub-mock',
          email: decoded.email || 'user@example.com',
          name: decoded.name || 'SkillBuilder User',
          picture: decoded.picture,
        };
      }
      throw new Error('Invalid ID token format');
    }
  }

  const ticket = await client.verifyIdToken({
    idToken,
    audience: ENV.GOOGLE_CLIENT_ID,
  });

  const payload = ticket.getPayload();
  if (!payload || !payload.email) {
    throw new Error('Google authentication token missing email payload');
  }

  return {
    googleId: payload.sub,
    email: payload.email,
    name: payload.name || payload.email.split('@')[0],
    picture: payload.picture,
  };
}
