import { Router } from 'express';
import { ChatController } from './chat.controller.js';
import { requireAuth } from '../../middlewares/auth.middleware.js';

export const chatRouter = Router();

chatRouter.get('/conversations', requireAuth, ChatController.getConversations);
chatRouter.get('/conversations/:conversationId/messages', requireAuth, ChatController.getMessages);
chatRouter.post('/messages', requireAuth, ChatController.sendMessage);
chatRouter.patch('/messages/:messageId/resolve', requireAuth, ChatController.resolveDoubt);
chatRouter.post('/messages/:messageId/vote', requireAuth, ChatController.upvoteDemand);
