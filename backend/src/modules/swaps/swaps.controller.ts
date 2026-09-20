import { Request, Response, NextFunction } from 'express';
import { SwapsService } from './swaps.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';
import { SwapStatus } from '@prisma/client';

const swapsService = new SwapsService();

export class SwapsController {
  static async getMatches(req: Request, res: Response, next: NextFunction) {
    try {
      const matches = await swapsService.findReciprocalMatches(req.user!.userId);
      return ApiResponse.success(res, matches, 'Reciprocal matches found');
    } catch (error) {
      next(error);
    }
  }

  static async createProposal(req: Request, res: Response, next: NextFunction) {
    try {
      const proposal = await swapsService.createProposal(req.user!.userId, req.body);
      return ApiResponse.created(res, proposal, 'Swap proposal created successfully');
    } catch (error) {
      next(error);
    }
  }

  static async getProposals(req: Request, res: Response, next: NextFunction) {
    try {
      const { status, type } = req.query;
      const proposals = await swapsService.getProposals(req.user!.userId, {
        status: status as SwapStatus,
        type: type as any,
      });
      return ApiResponse.success(res, proposals, 'Swap proposals fetched');
    } catch (error) {
      next(error);
    }
  }

  static async updateStatus(req: Request, res: Response, next: NextFunction) {
    try {
      const id = req.params.id as string;
      const { status } = req.body;
      const updated = await swapsService.updateProposalStatus(req.user!.userId, id, status);
      return ApiResponse.success(res, updated, `Swap proposal status updated to ${status}`);
    } catch (error) {
      next(error);
    }
  }
}
