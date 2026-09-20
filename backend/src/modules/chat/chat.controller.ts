import { Request, Response, NextFunction } from 'express';
import { ChatService } from './chat.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';

const chatService = new ChatService();

export class ChatController {
  static async getConversations(req: Request, res: Response, next: NextFunction) {
    try {
      const convs = await chatService.getConversations(req.user!.userId);
      return ApiResponse.success(res, convs, 'Conversations fetched');
    } catch (error) {
      next(error);
    }
  }

  static async getMessages(req: Request, res: Response, next: NextFunction) {
    try {
      const conversationId = req.params.conversationId as string;
      const messages = await chatService.getMessages(conversationId, req.user!.userId);
      return ApiResponse.success(res, messages, 'Messages fetched');
    } catch (error) {
      next(error);
    }
  }

  static async sendMessage(req: Request, res: Response, next: NextFunction) {
    try {
      const message = await chatService.sendMessage(req.user!.userId, req.body);
      return ApiResponse.created(res, message, 'Message sent successfully');
    } catch (error) {
      next(error);
    }
  }

  static async resolveDoubt(req: Request, res: Response, next: NextFunction) {
    try {
      const messageId = req.params.messageId as string;
      const updated = await chatService.resolveDoubt(messageId, req.user!.userId);
      return ApiResponse.success(res, updated, 'Doubt resolved');
    } catch (error) {
      next(error);
    }
  }

  static async upvoteDemand(req: Request, res: Response, next: NextFunction) {
    try {
      const messageId = req.params.messageId as string;
      const updated = await chatService.upvoteVideoDemand(messageId);
      return ApiResponse.success(res, updated, 'Demand upvoted');
    } catch (error) {
      next(error);
    }
  }
}
