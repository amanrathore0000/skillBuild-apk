import { Request, Response, NextFunction } from 'express';
import { WalletService } from './wallet.service.js';
import { ApiResponse } from '../../utils/apiResponse.js';

const walletService = new WalletService();

export class WalletController {
  static async getWallet(req: Request, res: Response, next: NextFunction) {
    try {
      const wallet = await walletService.getWallet(req.user!.userId);
      return ApiResponse.success(res, wallet, 'Wallet details fetched');
    } catch (error) {
      next(error);
    }
  }

  static async requestPayout(req: Request, res: Response, next: NextFunction) {
    try {
      const { amount, payoutMethod } = req.body;
      const result = await walletService.requestPayout(req.user!.userId, Number(amount), payoutMethod);
      return ApiResponse.success(res, result, 'Payout initiated successfully');
    } catch (error) {
      next(error);
    }
  }
}
