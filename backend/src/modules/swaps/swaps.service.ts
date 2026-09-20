import { prisma } from '../../config/database.js';
import { AppError } from '../../middlewares/error.middleware.js';
import { SwapStatus, SkillType } from '@prisma/client';

export class SwapsService {
  /**
   * Reciprocal Skill Swap Matching Algorithm
   * Finds bilateral matches where User A teaches what User B wants AND User B teaches what User A wants.
   */
  async findReciprocalMatches(userId: string) {
    // 1. Get current user's taught and wanted skills
    const mySkills = await prisma.userSkill.findMany({
      where: { userId },
      include: { skill: true },
    });

    const myTaughtSkillIds = mySkills
      .filter((s) => s.skillType === SkillType.TAUGHT)
      .map((s) => s.skillId);

    const myWantedSkillIds = mySkills
      .filter((s) => s.skillType === SkillType.WANTED)
      .map((s) => s.skillId);

    if (myTaughtSkillIds.length === 0 || myWantedSkillIds.length === 0) {
      return [];
    }

    // 2. Query partners who teach what I want AND want what I teach
    // Using Prisma relation query
    const potentialPartners = await prisma.user.findMany({
      where: {
        id: { not: userId },
        AND: [
          {
            userSkills: {
              some: {
                skillType: SkillType.TAUGHT,
                skillId: { in: myWantedSkillIds },
              },
            },
          },
          {
            userSkills: {
              some: {
                skillType: SkillType.WANTED,
                skillId: { in: myTaughtSkillIds },
              },
            },
          },
        ],
      },
      include: {
        userSkills: {
          include: { skill: true },
        },
      },
      take: 20,
    });

    // 3. Format matches with matching skill pairs
    return potentialPartners.map((partner) => {
      const partnerTeaches = partner.userSkills
        .filter((us) => us.skillType === SkillType.TAUGHT && myWantedSkillIds.includes(us.skillId))
        .map((us) => us.skill.title);

      const partnerWants = partner.userSkills
        .filter((us) => us.skillType === SkillType.WANTED && myTaughtSkillIds.includes(us.skillId))
        .map((us) => us.skill.title);

      return {
        partner: {
          id: partner.id,
          name: partner.name,
          email: partner.email,
          avatarUrl: partner.avatarUrl,
          location: partner.location,
          rating: Number(partner.rating),
          reviewCount: partner.reviewCount,
          isVerified: partner.isVerified,
        },
        matchedSkillsOfferedByPartner: partnerTeaches,
        matchedSkillsWantedByPartner: partnerWants,
        compatibilityScore: 100,
      };
    });
  }

  async createProposal(senderId: string, data: {
    receiverId: string;
    senderSkillId: string;
    receiverSkillId: string;
    proposedDate: string;
    message: string;
  }) {
    if (senderId === data.receiverId) {
      throw new AppError('Cannot create a swap proposal with yourself', 400);
    }

    // Verify skills exist
    const senderSkill = await prisma.skill.findUnique({ where: { id: data.senderSkillId } });
    const receiverSkill = await prisma.skill.findUnique({ where: { id: data.receiverSkillId } });

    if (!senderSkill || !receiverSkill) {
      throw new AppError('Invalid skill ID specified for swap proposal', 400);
    }

    return prisma.swapProposal.create({
      data: {
        senderId,
        receiverId: data.receiverId,
        senderSkillId: data.senderSkillId,
        receiverSkillId: data.receiverSkillId,
        proposedDate: new Date(data.proposedDate),
        message: data.message,
        status: SwapStatus.PENDING,
      },
      include: {
        sender: true,
        receiver: true,
        senderSkill: true,
        receiverSkill: true,
      },
    });
  }

  async getProposals(userId: string, filter?: { status?: SwapStatus; type?: 'sent' | 'received' }) {
    const where: any = {};

    if (filter?.type === 'sent') {
      where.senderId = userId;
    } else if (filter?.type === 'received') {
      where.receiverId = userId;
    } else {
      where.OR = [{ senderId: userId }, { receiverId: userId }];
    }

    if (filter?.status) {
      where.status = filter.status;
    }

    const proposals = await prisma.swapProposal.findMany({
      where,
      include: {
        sender: true,
        receiver: true,
        senderSkill: true,
        receiverSkill: true,
      },
      orderBy: { createdAt: 'desc' },
    });

    // Format proposals for Android domain model (SwapProposal)
    return proposals.map((p) => {
      const isSender = p.senderId === userId;
      const partner = isSender ? p.receiver : p.sender;
      const mySkill = isSender ? p.senderSkill.title : p.receiverSkill.title;
      const partnerSkill = isSender ? p.receiverSkill.title : p.senderSkill.title;

      return {
        id: p.id,
        partner: {
          id: partner.id,
          name: partner.name,
          email: partner.email,
          avatarUrl: partner.avatarUrl,
          phone: partner.phone,
          dob: partner.dob,
          location: partner.location,
          bio: partner.bio || '',
          rating: Number(partner.rating),
          reviewCount: partner.reviewCount,
          isVerified: partner.isVerified,
          isMentor: partner.isMentor,
        },
        mySkill,
        partnerSkill,
        status: p.status,
        proposedDate: p.proposedDate.toISOString(),
        message: p.message,
        isIncoming: !isSender,
      };
    });
  }

  async updateProposalStatus(userId: string, proposalId: string, status: SwapStatus) {
    const proposal = await prisma.swapProposal.findUnique({
      where: { id: proposalId },
    });

    if (!proposal) {
      throw new AppError('Swap proposal not found', 404);
    }

    if (proposal.receiverId !== userId && proposal.senderId !== userId) {
      throw new AppError('Unauthorized to modify this swap proposal', 403);
    }

    const updated = await prisma.swapProposal.update({
      where: { id: proposalId },
      data: { status },
      include: {
        sender: true,
        receiver: true,
        senderSkill: true,
        receiverSkill: true,
      },
    });

    // When proposal is accepted or active, automatically create or link the 1-on-1 private conversation
    if (status === SwapStatus.ACCEPTED || status === SwapStatus.ACTIVE) {
      const existingConv = await prisma.conversation.findFirst({
        where: {
          OR: [
            { mentorId: proposal.receiverId, learnerId: proposal.senderId },
            { mentorId: proposal.senderId, learnerId: proposal.receiverId },
          ],
        },
      });

      if (!existingConv) {
        await prisma.conversation.create({
          data: {
            mentorId: proposal.receiverId,
            learnerId: proposal.senderId,
            swapId: proposal.id,
            lastMessage: 'Swap accepted! You can now coordinate your sessions here.',
          },
        });
      }
    }

    return updated;
  }
}
