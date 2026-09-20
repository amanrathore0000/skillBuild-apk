import bcrypt from 'bcryptjs';
import { prisma } from '../../config/database.js';
import { verifyGoogleIdToken } from '../../utils/googleAuth.js';
import { generateTokens, verifyRefreshToken } from '../../utils/jwt.js';
import { AppError } from '../../middlewares/error.middleware.js';
export class AuthService {
    async authenticateWithGoogle(idToken) {
        const googleUser = await verifyGoogleIdToken(idToken);
        let user = await prisma.user.findFirst({
            where: {
                OR: [{ googleId: googleUser.googleId }, { email: googleUser.email }],
            },
            include: {
                userSkills: {
                    include: { skill: true },
                },
            },
        });
        if (!user) {
            user = await prisma.user.create({
                data: {
                    googleId: googleUser.googleId,
                    email: googleUser.email,
                    name: googleUser.name,
                    avatarUrl: googleUser.picture,
                    isVerified: true,
                    isMentor: false,
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
        }
        else if (!user.googleId) {
            user = await prisma.user.update({
                where: { id: user.id },
                data: {
                    googleId: googleUser.googleId,
                    avatarUrl: user.avatarUrl || googleUser.picture,
                },
                include: {
                    userSkills: {
                        include: { skill: true },
                    },
                },
            });
        }
        const tokens = generateTokens({
            userId: user.id,
            email: user.email,
            isMentor: user.isMentor,
        });
        return {
            user: this.formatUserResponse(user),
            ...tokens,
        };
    }
    async signup(data) {
        const existing = await prisma.user.findUnique({
            where: { email: data.email },
        });
        if (existing) {
            throw new AppError('An account with this email already exists', 409);
        }
        const passwordHash = await bcrypt.hash(data.password, 10);
        const user = await prisma.user.create({
            data: {
                email: data.email,
                passwordHash,
                name: data.name,
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
        const tokens = generateTokens({
            userId: user.id,
            email: user.email,
            isMentor: user.isMentor,
        });
        return {
            user: this.formatUserResponse(user),
            ...tokens,
        };
    }
    async login(data) {
        const user = await prisma.user.findUnique({
            where: { email: data.email },
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
        const tokens = generateTokens({
            userId: user.id,
            email: user.email,
            isMentor: user.isMentor,
        });
        return {
            user: this.formatUserResponse(user),
            ...tokens,
        };
    }
    async refreshToken(refreshToken) {
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
        }
        catch {
            throw new AppError('Invalid or expired refresh token', 401);
        }
    }
    async getCurrentUser(userId) {
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
    formatUserResponse(user) {
        const skillsTaught = (user.userSkills || [])
            .filter((us) => us.skillType === 'TAUGHT')
            .map((us) => us.skill.title);
        const skillsWanted = (user.userSkills || [])
            .filter((us) => us.skillType === 'WANTED')
            .map((us) => us.skill.title);
        return {
            id: user.id,
            name: user.name,
            email: user.email,
            avatarUrl: user.avatarUrl,
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
//# sourceMappingURL=auth.service.js.map