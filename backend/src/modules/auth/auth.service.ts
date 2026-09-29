import crypto from 'crypto';
import bcrypt from 'bcryptjs';
import { prisma } from '../../config/database.js';
import { verifyGoogleIdToken, type GoogleAuthOptions } from '../../utils/googleAuth.js';
import { generateTokens, verifyRefreshToken } from '../../utils/jwt.js';
import { AppError } from '../../middlewares/error.middleware.js';

export class AuthService {
  /**
   * Authenticates with Google OAuth 2.0 / OpenID Connect.
   * Follows strict account identity rules:
   * User └── Account └── Google OAuth identity (sub)
   * 1. If Account(providerId='google', accountId=sub) exists -> login to linked User.
   * 2. Else if User with verified email exists -> link Account to existing User -> login.
   * 3. Else -> create new User + create Account -> login.
   * 4. Creates a SkillBuilder database Session (7 days).
   * 5. Returns formatted user, sessionToken, and JWT tokens.
   */
  async authenticateWithGoogle(idToken: string, options?: GoogleAuthOptions) {
    const googlePayload = await verifyGoogleIdToken(idToken, options);

    let user: any = null;

    // Step 1: Check if an Account already exists for this Google provider identity (sub)
    const existingAccount = await prisma.account.findUnique({
      where: {
        providerId_accountId: {
          providerId: 'google',
          accountId: googlePayload.sub,
        },
      },
      include: {
        user: {
          include: {
            userSkills: { include: { skill: true } },
            wallet: true,
          },
        },
      },
    });

    if (existingAccount && existingAccount.user) {
      user = existingAccount.user;
      // Update user details if previously empty
      const updates: any = {};
      if (!user.avatarUrl && googlePayload.picture) updates.avatarUrl = googlePayload.picture;
      if (!user.image && googlePayload.picture) updates.image = googlePayload.picture;
      if (!user.emailVerified) updates.emailVerified = true;

      if (Object.keys(updates).length > 0) {
        user = await prisma.user.update({
          where: { id: user.id },
          data: updates,
          include: {
            userSkills: { include: { skill: true } },
            wallet: true,
          },
        });
      }

      // Update Account idToken and timestamp
      await prisma.account.update({
        where: { id: existingAccount.id },
        data: {
          idToken,
          updatedAt: new Date(),
        },
      });
    } else {
      // Step 2: Check if an existing User matches this verified Google email
      const existingUser = await prisma.user.findUnique({
        where: { email: googlePayload.email },
        include: {
          userSkills: { include: { skill: true } },
          wallet: true,
        },
      });

      if (existingUser) {
        // Explicit Account Linking: safely attach Google provider identity to existing user
        await prisma.account.create({
          data: {
            providerId: 'google',
            accountId: googlePayload.sub,
            userId: existingUser.id,
            idToken,
          },
        });

        // Ensure user is marked verified
        user = await prisma.user.update({
          where: { id: existingUser.id },
          data: {
            emailVerified: true,
            isVerified: true,
            avatarUrl: existingUser.avatarUrl || googlePayload.picture,
            image: existingUser.image || googlePayload.picture,
          },
          include: {
            userSkills: { include: { skill: true } },
            wallet: true,
          },
        });
      } else {
        // Step 3: Brand new user registration with Google
        user = await prisma.user.create({
          data: {
            email: googlePayload.email,
            name: googlePayload.name,
            emailVerified: true,
            avatarUrl: googlePayload.picture,
            image: googlePayload.picture,
            isVerified: true,
            isMentor: options?.isMentor || false,
            wallet: {
              create: {},
            },
            accounts: {
              create: {
                providerId: 'google',
                accountId: googlePayload.sub,
                idToken,
              },
            },
          },
          include: {
            userSkills: { include: { skill: true } },
            wallet: true,
          },
        });
      }
    }

    // Step 4: Create SkillBuilder database Session (7 days expiration)
    const sessionToken = crypto.randomBytes(32).toString('hex');
    const session = await prisma.session.create({
      data: {
        userId: user.id,
        token: sessionToken,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
        ipAddress: options?.ipAddress || null,
        userAgent: options?.userAgent || null,
      },
    });

    // Step 5: Generate JWT tokens for authenticated API requests
    const tokens = generateTokens({
      userId: user.id,
      email: user.email,
      isMentor: user.isMentor,
    });

    return {
      user: this.formatUserResponse(user),
      sessionToken: session.token,
      ...tokens,
    };
  }

  async signup(data: {
    email: string;
    password: string;
    name: string;
    isMentor?: boolean;
    location?: string;
    phone?: string;
    dob?: string;
  }) {
    const cleanEmail = data.email.trim().toLowerCase();
    const existing = await prisma.user.findUnique({
      where: { email: cleanEmail },
    });

    if (existing) {
      throw new AppError('An account with this email already exists', 409);
    }

    const passwordHash = await bcrypt.hash(data.password, 10);

    const user = await prisma.user.create({
      data: {
        email: cleanEmail,
        passwordHash,
        name: data.name.trim(),
        isMentor: data.isMentor || false,
        location: data.location || '',
        phone: data.phone || '',
        dob: data.dob || '',
        wallet: {
          create: {},
        },
      },
      include: {
        userSkills: {
          include: { skill: true },
        },
      },
    });

    const sessionToken = crypto.randomBytes(32).toString('hex');
    const session = await prisma.session.create({
      data: {
        userId: user.id,
        token: sessionToken,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      },
    });

    const tokens = generateTokens({
      userId: user.id,
      email: user.email,
      isMentor: user.isMentor,
    });

    return {
      user: this.formatUserResponse(user),
      sessionToken: session.token,
      ...tokens,
    };
  }

  async login(data: { email: string; password: string }) {
    const cleanEmail = data.email.trim().toLowerCase();
    const user = await prisma.user.findUnique({
      where: { email: cleanEmail },
      include: {
        userSkills: {
          include: { skill: true },
        },
      },
    });

    if (!user || !user.passwordHash) {
      throw new AppError('Invalid email or password credentials', 401);
    }

    const isMatch = await bcrypt.compare(data.password, user.passwordHash);
    if (!isMatch) {
      throw new AppError('Invalid email or password credentials', 401);
    }

    const sessionToken = crypto.randomBytes(32).toString('hex');
    const session = await prisma.session.create({
      data: {
        userId: user.id,
        token: sessionToken,
        expiresAt: new Date(Date.now() + 7 * 24 * 60 * 60 * 1000),
      },
    });

    const tokens = generateTokens({
      userId: user.id,
      email: user.email,
      isMentor: user.isMentor,
    });

    return {
      user: this.formatUserResponse(user),
      sessionToken: session.token,
      ...tokens,
    };
  }

  async logout(userId: string, sessionToken?: string) {
    if (sessionToken) {
      await prisma.session.deleteMany({
        where: { token: sessionToken },
      });
    } else {
      await prisma.session.deleteMany({
        where: { userId },
      });
    }
    return { success: true, message: 'Logged out successfully' };
  }

  async refreshToken(refreshToken: string) {
    try {
      const payload = verifyRefreshToken(refreshToken);
      const user = await prisma.user.findUnique({
        where: { id: payload.userId },
      });

      if (!user) {
        throw new AppError('User not found', 404);
      }

      const tokens = generateTokens({
        userId: user.id,
        email: user.email,
        isMentor: user.isMentor,
      });

      return tokens;
    } catch {
      throw new AppError('Invalid or expired refresh token', 401);
    }
  }

  async getCurrentUser(userId: string) {
    const user = await prisma.user.findUnique({
      where: { id: userId },
      include: {
        userSkills: {
          include: { skill: true },
        },
        wallet: true,
      },
    });

    if (!user) {
      throw new AppError('User not found', 404);
    }

    return this.formatUserResponse(user);
  }

  private formatUserResponse(user: any) {
    const skillsTaught = (user.userSkills || [])
      .filter((us: any) => us.skillType === 'TAUGHT')
      .map((us: any) => us.skill.title);

    const skillsWanted = (user.userSkills || [])
      .filter((us: any) => us.skillType === 'WANTED')
      .map((us: any) => us.skill.title);

    return {
      id: user.id,
      name: user.name,
      email: user.email,
      avatarUrl: user.avatarUrl || user.image,
      phone: user.phone || '',
      dob: user.dob || '',
      location: user.location || '',
      bio: user.bio || '',
      rating: Number(user.rating),
      reviewCount: user.reviewCount,
      isVerified: user.isVerified,
      isMentor: user.isMentor,
      skillsTaught,
      skillsWanted,
      wallet: user.wallet,
    };
  }
}
