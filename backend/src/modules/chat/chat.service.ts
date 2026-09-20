import { prisma } from '../../config/database.js';
import { AppError } from '../../middlewares/error.middleware.js';
import { ChatMessageType, MessageStatus } from '@prisma/client';

export class ChatService {
  async getConversations(userId: string) {
    const conversations = await prisma.conversation.findMany({
      where: {
        OR: [{ mentorId: userId }, { learnerId: userId }],
      },
      include: {
        mentor: true,
        learner: true,
      },
      orderBy: { lastMessageTimestamp: 'desc' },
    });

    return conversations.map((conv) => {
      const isMentor = conv.mentorId === userId;
      return {
        id: conv.id,
        mentorId: conv.mentorId,
        mentorName: conv.mentor.name,
        mentorAvatar: conv.mentor.avatarUrl,
        mentorCategory: 'General',
        learnerId: conv.learnerId,
        learnerName: conv.learner.name,
        learnerAvatar: conv.learner.avatarUrl,
        lastMessage: conv.lastMessage || '',
        lastMessageTimestamp: conv.lastMessageTimestamp.toISOString(),
        unreadCount: 0,
        hasDoubtPending: conv.hasDoubtPending,
        hasVideoDemandPending: conv.hasVideoDemandPending,
      };
    });
  }

  async getMessages(conversationId: string, userId: string) {
    const conv = await prisma.conversation.findUnique({
      where: { id: conversationId },
    });

    if (!conv || (conv.mentorId !== userId && conv.learnerId !== userId)) {
      throw new AppError('Conversation not found or unauthorized access', 403);
    }

    const messages = await prisma.chatMessage.findMany({
      where: { conversationId },
      include: {
        sender: {
          select: { id: true, name: true, isMentor: true },
        },
      },
      orderBy: { createdAt: 'asc' },
    });

    return messages.map((m) => ({
      id: m.id,
      conversationId: m.conversationId,
      senderId: m.senderId,
      senderName: m.sender.name,
      senderRole: m.sender.isMentor ? 'Mentor' : 'Learner',
      recipientId: m.recipientId,
      text: m.text,
      timestamp: m.createdAt.toISOString(),
      type: m.type,
      referenceTopic: m.referenceTopic,
      referenceCourseId: m.referenceCourseId,
      status: m.status,
      isEncrypted: m.isEncrypted,
      isResolved: m.isResolved,
      isAccepted: m.isAccepted,
      demandVotes: m.demandVotes,
    }));
  }

  async sendMessage(senderId: string, data: {
    conversationId: string;
    text: string;
    type?: ChatMessageType;
    referenceTopic?: string;
    referenceCourseId?: string;
  }) {
    const conv = await prisma.conversation.findUnique({
      where: { id: data.conversationId },
    });

    if (!conv) {
      throw new AppError('Conversation not found', 404);
    }

    const recipientId = conv.mentorId === senderId ? conv.learnerId : conv.mentorId;
    const msgType = data.type || ChatMessageType.STANDARD;

    const message = await prisma.chatMessage.create({
      data: {
        conversationId: data.conversationId,
        senderId,
        recipientId,
        text: data.text,
        type: msgType,
        referenceTopic: data.referenceTopic,
        referenceCourseId: data.referenceCourseId,
        status: MessageStatus.DELIVERED,
      },
      include: {
        sender: true,
      },
    });

    // Update conversation metadata
    await prisma.conversation.update({
      where: { id: data.conversationId },
      data: {
        lastMessage: data.text,
        lastMessageTimestamp: new Date(),
        hasDoubtPending: msgType === ChatMessageType.DOUBT_QUERY ? true : conv.hasDoubtPending,
        hasVideoDemandPending: msgType === ChatMessageType.VIDEO_DEMAND ? true : conv.hasVideoDemandPending,
      },
    });

    return {
      id: message.id,
      conversationId: message.conversationId,
      senderId: message.senderId,
      senderName: message.sender.name,
      senderRole: message.sender.isMentor ? 'Mentor' : 'Learner',
      recipientId: message.recipientId,
      text: message.text,
      timestamp: message.createdAt.toISOString(),
      type: message.type,
      referenceTopic: message.referenceTopic,
      referenceCourseId: message.referenceCourseId,
      status: message.status,
      isEncrypted: message.isEncrypted,
      isResolved: message.isResolved,
      isAccepted: message.isAccepted,
      demandVotes: message.demandVotes,
    };
  }

  async resolveDoubt(messageId: string, userId: string) {
    const message = await prisma.chatMessage.findUnique({
      where: { id: messageId },
      include: { conversation: true },
    });

    if (!message || (message.conversation.mentorId !== userId && message.conversation.learnerId !== userId)) {
      throw new AppError('Message not found or unauthorized', 403);
    }

    const updated = await prisma.chatMessage.update({
      where: { id: messageId },
      data: { isResolved: true },
    });

    // Check if any doubts remain in conversation
    const pendingDoubts = await prisma.chatMessage.count({
      where: {
        conversationId: message.conversationId,
        type: ChatMessageType.DOUBT_QUERY,
        isResolved: false,
      },
    });

    await prisma.conversation.update({
      where: { id: message.conversationId },
      data: { hasDoubtPending: pendingDoubts > 0 },
    });

    return updated;
  }

  async upvoteVideoDemand(messageId: string) {
    return prisma.chatMessage.update({
      where: { id: messageId },
      data: {
        demandVotes: { increment: 1 },
      },
    });
  }
}
