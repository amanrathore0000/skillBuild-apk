import { prisma } from '../../config/database.js';
import { AppError } from '../../middlewares/error.middleware.js';
import { SkillType } from '@prisma/client';
export class UsersService {
    async getProfile(userId) {
        const user = await prisma.user.findUnique({
            where: { id: userId },
            include: {
                userSkills: {
                    include: { skill: true },
                },
                _count: {
                    select: {
                        courses: true,
                        sentProposals: true,
                        receivedProposals: true,
                    },
                },
            },
        });
        if (!user) {
            throw new AppError('User not found', 404);
        }
        const skillsTaught = user.userSkills
            .filter((us) => us.skillType === SkillType.TAUGHT)
            .map((us) => us.skill.title);
        const skillsWanted = user.userSkills
            .filter((us) => us.skillType === SkillType.WANTED)
            .map((us) => us.skill.title);
        return {
            id: user.id,
            name: user.name,
            email: user.email,
            avatarUrl: user.avatarUrl,
            phone: user.phone,
            dob: user.dob,
            location: user.location,
            bio: user.bio,
            rating: Number(user.rating),
            reviewCount: user.reviewCount,
            isVerified: user.isVerified,
            isMentor: user.isMentor,
            skillsTaught,
            skillsWanted,
            counts: user._count,
        };
    }
    async updateProfile(userId, data) {
        const updated = await prisma.user.update({
            where: { id: userId },
            data,
            include: {
                userSkills: {
                    include: { skill: true },
                },
            },
        });
        return this.getProfile(updated.id);
    }
    async updateUserSkills(userId, data) {
        // Transaction to update user skills cleanly
        await prisma.$transaction(async (tx) => {
            // Clear existing
            await tx.userSkill.deleteMany({
                where: { userId },
            });
            // Insert taught skills
            if (data.taughtSkillIds && data.taughtSkillIds.length > 0) {
                await tx.userSkill.createMany({
                    data: data.taughtSkillIds.map((skillId) => ({
                        userId,
                        skillId,
                        skillType: SkillType.TAUGHT,
                    })),
                });
            }
            // Insert wanted skills
            if (data.wantedSkillIds && data.wantedSkillIds.length > 0) {
                await tx.userSkill.createMany({
                    data: data.wantedSkillIds.map((skillId) => ({
                        userId,
                        skillId,
                        skillType: SkillType.WANTED,
                    })),
                });
            }
        });
        return this.getProfile(userId);
    }
    async getMentorStats(userId) {
        const wallet = await prisma.mentorWallet.findUnique({
            where: { userId },
        });
        const activeStudents = await prisma.enrollment.count({
            where: {
                course: { mentorId: userId },
            },
        });
        const user = await prisma.user.findUnique({
            where: { id: userId },
            select: { rating: true },
        });
        return {
            totalRevenue: wallet ? `$${Number(wallet.totalEarned).toFixed(2)}` : '$0.00',
            activeStudents,
            totalHoursTaught: 48,
            averageRating: user ? Number(user.rating) : 5.0,
        };
    }
}
//# sourceMappingURL=users.service.js.map